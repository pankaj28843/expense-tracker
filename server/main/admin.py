from django.contrib import admin
from main.models import *
from main.forms import OrgAddForm

class ProjectInline(admin.TabularInline):
    model = Project
    extra = 2

class OrganisationAdmin(admin.ModelAdmin):

    def queryset(self, request):
        qs = super(OrganisationAdmin, self).queryset(request)
        if request.user.is_superuser:
            return qs
        return qs


    fieldsets = [
                (None,   {
                            'fields': ['title', 'admins', 'users',
                                       'locations', ],
                         }
                ),
    ]

    add_fieldsets = [
                (None,   {
                            'fields': ['title', 'admins'],
                         }
                ),
    ]

    edit_fieldsets = [
                (None,   {
                            'fields': ['title', 'users',
                                       'locations'],
                         }
                ),
    ]

    add_form = OrgAddForm
    #readonly_fields = (,)
    filter_horizontal = ('admins', 'users', 'locations')
    list_display = ('title', 'id')
    list_filter = ['title']
    search_fields = ['title']
    inlines = [ProjectInline]
    #date_hierarchy = 'time'

    def queryset(self, request):
        if request.user.is_superuser:
            return super(OrganisationAdmin, self).queryset(request)
        #Allow admins to view orgs only which they manage
        return request.user.managed.all()

    def get_fieldsets(self, request, obj=None):
        if request.user.has_perms('main.organisation'):#is_superuser:
            return super(OrganisationAdmin, self).get_fieldsets(request, obj)
        if not obj:
            return self.add_fieldsets
        else:
            return self.edit_fieldsets

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

class ExpenseAdmin(admin.ModelAdmin):
    def queryset(self, request):
        qs = super(ExpenseAdmin, self).queryset(request)
        if request.user.has_perms('main.expense'):
            return qs
        return qs.filter(type=OFFICIAL,
                         project__organisation__in=request.user.managed.all())

    fieldsets = [
            (None, {
                'fields':['user' , 'organisation', 'type', 'amount', 'location',
                          'category', 'time', 'add_time'],
            }),
            ('Meta', {
                'fields': ['project', 'billed', 'bill_id', 'bill_image',
                           'description'],
            },)
    ]

    edit_fieldset = [
            (None, {
                'fields':['user', 'organisation', 'amount', 'location',
                          'category', 'time', 'add_time'],
            }),
            ('Meta', {
                'fields': ['billed', 'bill_id', 'bill_image',
                           'description'],
            },)
    ]

    readonly_fields = ('user', 'organisation', 'add_time')
    #filter_horizontal = (,)
    list_display = ('user', 'organisation', 'amount', 'type', 'location',
                    'category', 'time')
    list_filter = ['token__user__username', 'project__organisation__title',
                   'location', 'category', 'project']
    list_select_related = True
    search_fields = ['token__user__username', 'project__organisation__title',
                     'location__title', 'category__title', 'project__title']
    date_hierarchy = 'time'

    def get_fieldsets(self, request, obj=None):
        if request.user.has_perms('main.expense'):
            return super(ExpenseAdmin, self).get_fieldsets(request, obj)
        else:
            return self.edit_fieldset

admin.site.register(Category, CategoryAdmin)
admin.site.register(Location, LocationAdmin)
admin.site.register(Organisation, OrganisationAdmin)
admin.site.register(Expense, ExpenseAdmin)
