# -*- coding: utf-8 -*-
from django.db import models
from django.db.models.query import QuerySet
from django.core.urlresolvers import reverse

from django.contrib.auth.models import User

from random import random
from hashlib import sha1

#Type of expenses
PERSONAL = 'p'
OFFICIAL = 'o'

# This is based on the snippet:
# http://djangosnippets.org/snippets/734/
class ExpenseQuerySet(QuerySet):
    '''
    The methods provide statistics according to the queryset.
    '''

    def stats(self, field):
        field_values = self.values_list(field, flat=True).distinct(field)
        st = []
        for val in field_values:
            qs = self.filter(**{field:val})
            sum_amount = qs.aggregate(models.Sum('amount'))['amount__sum']
            st.append({
                'field_value': val,
                'sum':sum_amount,
            })
        return st

    def stats_string(self, field, currency):
        stats = self.stats(field)
        stat_list = ['%s: %s %s' %(stat['field_value'], currency,
                                  stat['sum'])
                     for stat in stats]
        return ', '.join(stat_list) or 'No expense'

    def total(self):
        sum_amount = self.aggregate(models.Sum('amount'))['amount__sum']
        return sum_amount


class ExpenseManager(models.Manager):
    """
    Manager for expenditure.
    """

    def get_query_set(self):
        return ExpenseQuerySet(self.model)


class Location(models.Model):
    title = models.CharField(max_length=200, unique=True)

    def __unicode__(self):
        return self.title

class Organisation(models.Model):
    CURRENCIES = (
        ('Rs.', 'Indian Rupees (Rs.)'),
        (u'$', 'USD ($)'),
        (u'£', 'Pound (£)'),
        (u'€', 'Euro (€)'),
        (u'¥', 'Yen (¥)'),
    )
    title = models.CharField(max_length=200, unique=True)
    ## These users have the administration access for the org
    admins = models.ManyToManyField(User, related_name='managed',
                                    blank=True)
    ## The currency which is used by the organisation
    ## TODO: Move it to expense
    currency = models.CharField(max_length=10, choices=CURRENCIES,
                                default='Rs.')
    users = models.ManyToManyField(User, blank=True)
    locations = models.ManyToManyField(Location)

    def __unicode__(self):
        return self.title

    def get_absolute_url(self):
        return reverse('organisation', kwargs={'org_pk': self.pk})

    def get_currency(self):
        return self.currency

    def _get_stats(self, field, *args):
        """
        Get expense stats for project expenses
        """
        project_set = self.project_set.all()
        expense_set = Expense.objects.filter(project__in=project_set)
        stats = expense_set.stats_string(field, self.get_currency())
        return stats

    def category_stats(self):
        return self._get_stats('category__title')

    def user_stats(self):
        return self._get_stats('token__user__username')

    def location_stats(self):
        return self._get_stats('location__title')

    def total_spent(self):
        project_set = self.project_set.all()
        expense_set = Expense.objects.filter(project__in=project_set)
        total = expense_set.total()
        if total:
            return_string = '%s%s' %(self.get_currency(), total)
        else:
            return_string = 'No expense till now'
        return return_string

class Project(models.Model):
    title = models.CharField(max_length=200, unique=True)
    organisation = models.ForeignKey(Organisation)

    class Meta:
        #ordering = ['title']
        pass

    def __unicode__(self):
        return self.title

    def get_currency(self):
        return self.organisation.get_currency()
    get_currency.short_description = 'Currency'

    def _get_stats(self, field, *args):
        """
        Get expense stats for project expenses
        """
        expense_set = self.expense_set.all()
        stats = expense_set.stats_string(field, self.get_currency())
        return stats

    def category_stats(self):
        return self._get_stats('category__title')

    def user_stats(self):
        return self._get_stats('token__user__username')

    def location_stats(self):
        return self._get_stats('location__title')

    def total_spent(self):
        total = self.expense_set.all().total()
        if total:
            return_string = '%s%s' %(self.get_currency(), total)
        else:
            return_string = 'No expense till now'
        return return_string

class Category(models.Model):
    '''
    Expense Category
    '''

    title = models.CharField(max_length=200, unique=True)

    class Meta:
        verbose_name_plural = "categories"

    def __unicode__(self):
        return self.title

class AuthToken(models.Model):
    """
    A unique token given to mobile devices to track what device is used
    to upload the data.
    Also for uploading from websites, use a separate type of site token.
    """
    user = models.ForeignKey(User)
    key = models.CharField(max_length=400, editable=False)
    time = models.DateTimeField(auto_now_add=True)
    site_token = models.BooleanField(default=False, editable=False)

    def __unicode__(self):
        return '%s - %s token (%s)' % (self.user, self.type(), self.id)

    def type(self):
        """
        Returns type of token - On site/Mobile device
        """
        return 'On site' if self.site_token else 'Mobile device'

    def _set_key(self):
        salt = random()
        string = '%s%s-%s' %(self.user.username, self.user.password, salt)
        return sha1(string).hexdigest()

    def save(self, *args, **kwargs):
        if not self.key:
            self.key = self._set_key()
        return super(AuthToken, self).save(*args, **kwargs)

class Expense(models.Model):
    """
    An expense. Recordes the amount and other details of an expenditure.
    """

    TYPE_CHOICES = (
                (PERSONAL, 'Personal'),
                (OFFICIAL, 'Official')
            )
    project = models.ForeignKey(Project, blank=True, null=True)
    category = models.ForeignKey(Category)
    location = models.ForeignKey(Location)
    type = models.CharField(max_length=400, choices=TYPE_CHOICES)
    billed = models.BooleanField(default=False)
    amount = models.FloatField()
    token = models.ForeignKey(AuthToken)
    bill_id = models.CharField(max_length=100, blank=True, null=True)
    bill_image = models.ImageField(upload_to='bills/', blank=True, null=True)
    add_time = models.DateTimeField(auto_now_add=True)
    time = models.DateTimeField()
    description = models.TextField(blank=True, help_text="Enter extra details here, if any")

    objects = ExpenseManager()

    class Meta:
        get_latest_by = 'add_time'
        #ordering = ('-add_time',)

    def __unicode__(self):
        return '%s - %s' %(self.token.user, self.get_type_display())

    def user(self):
        return self.token.user

    def organisation(self):
        return self.project.organisation if self.project else '-'*16

    def data_tuple(self):
        return (
            unicode(self.user()),
            self.amount,
            unicode(self.location),
            unicode(self.category),
            unicode(self.organisation()),
            unicode(self.project),
            self.time.date().isoformat(),
        )

def create_csv(*args):
    """
    Create a comma and `|` separated string according to the list supplied.
    """
    final_list = []
    for arg in args:
        if type(arg)==list:
            arg = ','.join(arg)
        else:
            arg = str(arg)
        final_list.append(arg)

    return '|'.join(final_list)

def get_list(queryset, attribute='title', order_attrib='title'):
    """
    Get a list of values an attribute of queryset.
    """
    queryset = queryset.order_by(order_attrib)
    return map(lambda x:str(getattr(x, attribute)), queryset)

def get_by_title(model, title):
    """
    Get object or None according to title supplied
    """
    try:
        obj = model.objects.get(title__iexact=title)
    except:
        obj = None
    return obj

def get_sync_data(auth_token):
    """
    Create sync data string to be provided to mobile devices.
    Format:
    uid|token|projects(csv)|project_ids(csv)|type(csv)|locations(csv)|last bill or empty
    """
    user = auth_token.user
    orgs = user.organisation_set.all()
    project_set = Project.objects.filter(
        organisation__in=orgs)
    location_set = Location.objects.filter(organisation__in=orgs).distinct('title')
    categories = get_list(Category.objects.all())
    projects = get_list(project_set)
    project_ids = get_list(project_set, 'id')
    locations = get_list(location_set)

    bills_count = Expense.objects.filter(token=auth_token,
                                         billed=True).count()

    data = create_csv(user.id, auth_token.key, auth_token.id,
                      projects, project_ids, categories, locations,
                      bills_count)
    return data
