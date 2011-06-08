from django.contrib import admin
from excel_response import ExcelResponse
from main.models import *
from main.forms import AdminExpenseForm


class LocationAdmin(admin.ModelAdmin):

    fieldsets = [
            (None, {
                'fields':['title'],
            })
    ]

    #readonly_fields = (,)
    #filter_horizontal = (,)
    list_display = ('title', 'id')
    list_filter = ['title']
    search_fields = ['title']
    #date_hierarchy = 'time'

class CategoryAdmin(admin.ModelAdmin):
    fieldsets = [
            (None, {
                'fields':['title'],
            })
    ]

    #readonly_fields = (,)
    #filter_horizontal = (,)
    list_display = ('title', 'id')
    list_filter = ['title']
    search_fields = ['title']
    #date_hierarchy = 'time'

class ProjectInline(admin.TabularInline):
    """
    Inline project administration for adding projects
    """
    model = Project
    extra = 1

    fields = ['title', 'currency']

class OrganisationAdmin(admin.ModelAdmin):
    """
    Organisation management. Restricted to organisation admins
    """

    fieldsets = [
        (None,   {
                    'fields': ['title', 'admins', 'users',
                               'locations', ],
                 }
        ),
    ]

    readonly_fields = ()
    filter_horizontal = ('users',)
    list_display = ('title', 'id')
    list_filter = ['title']
    search_fields = ['title']
    #date_hierarchy = 'time'
    inlines = [ProjectInline,]

    def queryset(self, request):
        if request.user.is_superuser:
            return super(OrganisationAdmin, self).queryset(request)
        #Allow admins to view orgs only which they manage
        return request.user.managed.all()


class ProjectAdmin(admin.ModelAdmin):
    """
    Detailed project admin for managing project
    """
    fieldsets = [
        (None, {
            'fields':['title', 'organisation', 'currency'],
        }),
        ('Stats',{
            'fields':['category_stats', 'user_stats', 'location_stats'],
        })
    ]

    readonly_fields = ['organisation', 'category_stats', 'user_stats',
                       'location_stats']
    list_display = ['title', 'organisation', 'currency', 'total_spent']
    list_filter = ['currency']

    def queryset(self, request):
        qs = super(ProjectAdmin, self).queryset(request)
        if request.user.has_perms('main.project'):
            return qs
        else:
            return qs.filter(organisation__in=request.user.organisation_set.all())


def export_as_xls(modeladmin, request, queryset):
    data = [(
        'User',
        'Amount',
        'Location',
        'Category',
        'Organisation',
        'Project',
        'Time',
    )]
    for expense in queryset:
        data.append(expense.data_tuple())
    return ExcelResponse(data)
export_as_xls.short_description = 'Export as spreadsheet'

class ExpenseAdmin(admin.ModelAdmin):
    """
    Expenditure details administration. Restricted to official expeneses
    and accessible to organisation admins only.
    """
    fieldsets = [
            (None, {
                'fields':['user' , 'type', 'amount', 'location',
                          'category', 'time', 'add_time'],
            }),
            ('Meta', {
                'fields': ['organisation', 'project', 'billed', 'bill_id', 
                           'bill_image', 'description'],
            },)
    ]

    form = AdminExpenseForm

    actions = [export_as_xls]

    readonly_fields = ['user', 'organisation', 'add_time', 'billed', 'bill_id']
    #filter_horizontal = (,)
    list_display = ('user', 'organisation', 'amount', 'type', 'location',
                    'category', 'project', 'time')
    list_filter = ['project__organisation__title',
                   'location', 'category', 'project']
    list_select_related = True
    search_fields = ['token__user__username', 'project__organisation__title',
                     'location__title', 'category__title', 'project__title']
    date_hierarchy = 'time'

    def queryset(self, request):
        qs = super(ExpenseAdmin, self).queryset(request)
        if request.user.has_perms('main.expense'):
            return qs
        return qs.filter(type=OFFICIAL,
                         project__organisation__in=request.user.managed.all())

    def get_readonly_fields(self, request, obj=None):
        fields = super(ExpenseAdmin, self).get_readonly_fields(request, obj)
        if request.user.has_perms('main.expense'):
            return fields
        else:
            # General admins cannot edit project
            fields.append('project')
            return fields

    def get_fieldsets(self, request, obj=None):
        return super(ExpenseAdmin, self).get_fieldsets(request, obj)

    def get_form(self, request, obj=None):
        f = super(ExpenseAdmin, self).get_form(request, obj)
        if request.user.is_superuser:
            return f
        return f
        if obj is None:
            f.project.queryset=Project.objects.filter(organisation__in=user.organisation_set.all())
        else:
            f.project.queryset=Project.objects.filter(organisation=obj.organisation)
        return f


admin.site.register(Category, CategoryAdmin)
admin.site.register(Location, LocationAdmin)
admin.site.register(Organisation, OrganisationAdmin)
admin.site.register(Project, ProjectAdmin)
admin.site.register(Expense, ExpenseAdmin)
