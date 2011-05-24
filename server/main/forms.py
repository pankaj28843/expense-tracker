from django import forms
from django.contrib.admin.widgets import AdminSplitDateTime
from django.forms import ModelChoiceField, ChoiceField
from django.forms.models import ModelChoiceIterator

from main.models import Expense, PERSONAL, OFFICIAL, Category, Location, \
        Project, Organisation

from datetime import datetime

class ExpenseMixin(forms.ModelForm):
    """
    Common form for personal and official form
    """
    category = forms.ModelChoiceField(queryset=Category.objects.all(),
                                      required=True,)
    time = forms.DateTimeField(initial=datetime.now(),)
                               #widget=forms.SplitDateTimeWidget())

class PersonalExpenseForm(ExpenseMixin, forms.ModelForm):
    '''
    Personal expense form
    '''
    type = forms.CharField(initial=PERSONAL, widget=forms.HiddenInput())
    location = forms.ModelChoiceField(queryset=Location.objects.all(),)

    class Meta:
        model = Expense
        fields=('time', 'amount', 'category', 'location', 'description')

class OfficialExpenseForm(ExpenseMixin, forms.ModelForm):
    '''
    Official Expense form, requires organisation as an argument
    '''
    type = forms.CharField(initial=OFFICIAL, widget=forms.HiddenInput())
    project = forms.ModelChoiceField(queryset=Project.objects.all(),
                                     required=False)

    def __init__(self, org, *args, **kwargs):
        super(OfficialExpenseForm, self).__init__(*args, **kwargs)
        self.fields['project'].queryset = org.project_set.all()

    class Media:
        js = ['js/add_form.js']

    class Meta:
        model = Expense
        fields = ('time', 'amount', 'category', 'location', 'project',
                  'billed', 'description', 'type',)


class AdminExpenseForm(forms.ModelForm):
    class Meta:
        model = Expense
