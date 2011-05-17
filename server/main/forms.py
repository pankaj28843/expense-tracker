from django import forms
from django.contrib.admin.widgets import AdminSplitDateTime

from main.models import *

from datetime import datetime

class ExpenseForm(forms.ModelForm):
    location = forms.ModelChoiceField(queryset=Location.objects.all(),)
    project = forms.ModelChoiceField(queryset=Project.objects.all(), 
                                     required=False,)
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


