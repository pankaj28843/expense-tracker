from django.db import models
from django.contrib.auth.models import User
from django import forms

from random import randint
from hashlib import sha1

class Location(models.Model):
    title = models.CharField(max_length=200, unique=True)

    def __unicode__(self):
        return self.title

class Organisation(models.Model):
    title = models.CharField(max_length=200, unique=True)
    users = models.ManyToManyField(User)
    locations = models.ManyToManyField(Location)

    def __unicode__(self):
        return self.title

class Project(models.Model):
    title = models.CharField(max_length=200, unique=True)
    organisation = models.ForeignKey(Organisation)

    def __unicode__(self):
        return self.title

class Category(models.Model):
    title = models.CharField(max_length=200, unique=True)

    class Meta:
        verbose_name_plural = "categories"

    def __unicode__(self):
        return self.title

class AuthToken(models.Model):
    user = models.ForeignKey(User)
    organisation = models.ForeignKey(Organisation, default=1)
    key = models.CharField(max_length=400, editable=False)
    time = models.DateTimeField(auto_now_add=True)
    site_token = models.BooleanField(default=False, editable=False)

    def __unicode__(self):
        return '%s - %s (%s)' % (self.user, self.organisation, self.id)

    def set_key(self):
        salt = randint(1, 99999)
        string = '%s%s-%s' %(self.user.id, self.organisation, salt)
        return sha1(string).hexdigest()

    def save(self, *args, **kwargs):
        if not self.key:
            self.key = self.set_key()
        return super(AuthToken, self).save(*args, **kwargs)

class Expense(models.Model):
    TYPE_CHOICES = (
                ('Personal', 'Personal'),
                ('Official', 'Official')
            )
    project = models.ForeignKey(Project, blank=True, null=True)
    category = models.ForeignKey(Category, blank=True, null=True)
    location = models.ForeignKey(Location)
    type = models.CharField(max_length=400, choices=TYPE_CHOICES)
    billed = models.BooleanField(default=False)
    amount = models.FloatField()
    token = models.ForeignKey(AuthToken)
    bill_id = models.CharField(max_length=100)
    bill_image = models.ImageField(upload_to='bills/', blank=True, null=True)
    add_time = models.DateTimeField(auto_now_add=True)

    class Meta:
        get_latest_by = 'add_time'
        ordering = ('-add_time',)

    def __unicode__(self):
        return '%s spent %s %s (%s) at %s' %(
                self.token.user, self.amount, self.get_type_string(),
                self.token.organisation, self.location)

    def get_type_string(self):
        if self.type=='personal':
            return 'as personal expense' %(self.token.organisation)
        else:
            return 'for %s - %s' %(self.project, self.category)

    def user(self):
        return self.token.user

    def organisation(self):
        return self.token.organisation

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
    organisation = auth_token.organisation
    categories = get_list(Category.objects.all())
    projects = get_list(organisation.project_set.all())
    project_ids = get_list(organisation.project_set.all(), 'id')
    locations = get_list(organisation.locations.all())

    bills_count = Expense.objects.filter(token=auth_token,
                                         billed=True).count()

    data = create_csv(auth_token.user.id, auth_token.key, auth_token.id,
                      projects, project_ids, categories, locations,
                      bills_count)
    return data
