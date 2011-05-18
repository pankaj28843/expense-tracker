from django import forms
from django.contrib.admin.widgets import AdminSplitDateTime
from django.forms import ModelChoiceField, ChoiceField
from django.forms.models import ModelChoiceIterator

from main.models import *

from datetime import datetime

class ExpenseForm(forms.ModelForm):
    location = forms.ModelChoiceField(queryset=Location.objects.all(),)
    project = forms.ModelChoiceField(queryset=Project.objects.all(),)
    category = forms.ModelChoiceField(queryset=Category.objects.all(),
                                      required=True,)
    time = forms.DateTimeField(initial=datetime.now(),
                               widget=forms.SplitDateTimeWidget())

    class Media:
        css = {
            'all':('css/add_form.css',),
        }
        js = ('js/add_form.js',)

    class Meta:
        model = Expense
        fields = ('amount', 'location', 'type', 'category', 'project',
                  'billed', 'bill_image', 'time')

    def __init__(self, *args, **kwargs):
        return super(ExpenseForm, self).__init__(*args, **kwargs)

    def update_querysets(self, user):
        f = self.fields
        f['project'].queryset = f['project'].queryset.filter(
            organisation__in=user.organisation_set.all())
        self.fields = f


        #return (
            #('Audio', (
                    #('vinyl', 'Vinyl'),
                    #('cd', 'CD'),
                #)
            #),
            #('Video', (
                    #('vhs', 'VHS Tape'),
                    #('dvd', 'DVD'),
                #)
            #),
            #('unknown', 'Unknown'),
        #)

    #def _set_chocies(self, value):
        #pass

