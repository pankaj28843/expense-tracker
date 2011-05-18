from django import forms
from django.contrib.admin.widgets import AdminSplitDateTime
from django.forms import ModelChoiceField, ChoiceField
from django.forms.models import ModelChoiceIterator

from main.models import Expense, PERSONAL, OFFICIAL, Category, Location, \
        Project

from datetime import datetime

class ExpenseMixin(forms.ModelForm):
    category = forms.ModelChoiceField(queryset=Category.objects.all(),
                                      required=True,)
    time = forms.DateTimeField(initial=datetime.now(),
                               widget=forms.SplitDateTimeWidget())

class PersonalExpenseForm(ExpenseMixin, forms.ModelForm):
    type = forms.CharField(initial=PERSONAL, widget=forms.HiddenInput())
    location = forms.ModelChoiceField(queryset=Location.objects.all(),)

    class Meta:
        model = Expense
        fields=('amount', 'location', 'category', 'type', 'time', 'description')

class OfficialExpenseForm(ExpenseMixin, forms.ModelForm):
    type = forms.CharField(initial=OFFICIAL, widget=forms.HiddenInput())
    project = forms.ModelChoiceField(queryset=Project.objects.all(),
                                     required=False)

    def update_querysets(self, org):
        f = self.fields
        f['project'].queryset = org.project_set.all()
        self.fields = f

    class Meta:
        model = Expense
        fields = ('amount', 'location', 'type', 'category', 'project',
                  'billed', 'bill_image', 'time', 'description')


