from django import forms
from main.models import *

from datetime import datetime

class ExpenseForm(forms.ModelForm):
    location = forms.ModelChoiceField(queryset=Location.objects.all(),)
#    time = forms.DateTimeField(initial=datetime.now(),
#                               widget=forms.HiddenInput())
    project = forms.ModelChoiceField(queryset=Project.objects.all(), 
                                     required=False,)
    category = forms.ModelChoiceField(queryset=Category.objects.all(),
                                      required=False,)

    class Media:
        js = ('js/add_form.js',)

    class Meta:
        model = Expense
        fields = ('amount', 'location', 'type', 'project', 'category',
                  'billed', 'bill_image')

    def __init__(self, *args, **kwargs):
        return super(ExpenseForm, self).__init__(*args, **kwargs)


