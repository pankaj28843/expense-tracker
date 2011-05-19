# -*- coding: utf-8 -*-
from django.db import models
from django import forms
from django.core.urlresolvers import reverse

from django.contrib.auth.models import User

from random import random
from hashlib import sha1

PERSONAL = 'p'
OFFICIAL = 'o'

class ExpenseManager(models.Manager):
    def stats(self, currency):
        categories = self.values_list('category__title', flat=True).distinct('category')
        st = []
        for category in categories:
            st.append(('%s: %s %s') %(category, currency,
                                     self.aggregate(models.Sum('amount'))['amount__sum']))

        return ', '.join(st) or 'No expenses till now'

class Location(models.Model):
    title = models.CharField(max_length=200, unique=True)

    def __unicode__(self):
        return self.title

class Organisation(models.Model):
    title = models.CharField(max_length=200, unique=True)
    admins = models.ManyToManyField(User, related_name='managed',
                                    blank=True)
    users = models.ManyToManyField(User, blank=True)
    locations = models.ManyToManyField(Location)

    def __unicode__(self):
        return self.title

    def get_absolute_url(self):
        return reverse('organisation', kwargs={'org_pk': self.pk})

class Project(models.Model):
    CURRENCIES = (
        ('Rs.', 'Indian Rupees (Rs.)'),
        (u'$', 'USD ($)'),
        (u'£', 'Pound (£)'),
        (u'€', 'Euro (€)'),
        (u'¥', 'Yen (¥)'),
    )
    title = models.CharField(max_length=200, unique=True)
    organisation = models.ForeignKey(Organisation)
    currency = models.CharField(max_length=10, choices=CURRENCIES)
    #budget = models.IntegerField()

    class Meta:
        #ordering = ['title']
        pass

    def __unicode__(self):
        return self.title

    def expense_stats(self):
        return self.expense_set.stats(self.currency)

class Category(models.Model):
    title = models.CharField(max_length=200, unique=True)

    class Meta:
        verbose_name_plural = "categories"

    def __unicode__(self):
        return self.title

class AuthToken(models.Model):
    user = models.ForeignKey(User)
    key = models.CharField(max_length=400, editable=False)
    time = models.DateTimeField(auto_now_add=True)
    site_token = models.BooleanField(default=False, editable=False)

    def __unicode__(self):
        return '%s - %s token (%s)' % (self.user, self.type(), self.id)

    def type(self):
        return 'On site' if self.site_token else 'Mobile device'

    def set_key(self):
        salt = random()
        string = '%s%s-%s' %(self.user.username, self.user.password, salt)
        return sha1(string).hexdigest()

    def save(self, *args, **kwargs):
        if not self.key:
            self.key = self.set_key()
        return super(AuthToken, self).save(*args, **kwargs)

class Expense(models.Model):
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

def create_csv(*args):
    final_list = []
    for arg in args:
        if type(arg)==list:
            arg = ','.join(arg)
        else:
            arg = str(arg)
        final_list.append(arg)

    return '|'.join(final_list)

def get_list(queryset, attribute='title', order_attrib='title'):
    queryset = queryset.order_by(order_attrib)
    return map(lambda x:str(getattr(x, attribute)), queryset)

def get_by_title(model, title):
    try:
        obj = model.objects.get(title__iexact=title)
    except:
        obj = None
    return obj

def get_sync_data(auth_token):
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
