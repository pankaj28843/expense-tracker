from django.core.serializers.json import DjangoJSONEncoder
from django.http import HttpResponse, HttpResponseRedirect, Http404
from django.shortcuts import render, get_object_or_404, redirect
from django.template import RequestContext
from django.utils import simplejson

from django.contrib.auth.models import User, check_password

from main.models import *
from main.forms import ExpenseForm

from datetime import datetime
import settings

def home(request):
    if request.user.is_authenticated():
        expenses = Expense.objects.filter(token__user=request.user)

        if request.method == 'POST':
            form = ExpenseForm(request.POST, request.FILES)
            if request.POST['type']=='Official':
                form.fields['project'].required = True
                print form.fields['time']

            if form.is_valid():
                expense = form.save(commit=False)
                token = AuthToken.objects.get_or_create(user=request.user,
                                  site_token=True)[0]
                expense.token = token
                if expense.billed:
                    expense.bill_id = "%s%s%s%s"%(token.user.id,
                                                  expense.project.id,
                                                  expense.token.id,
                                                  expenses.filter(token=token).count()+1)
                expense.save()

                return redirect('/')
        else:
            initial = {
            }
            try:
                latest = expenses.latest()
                initial['location'] = latest.location
                initial['type']=latest.type
                initial['category']=latest.category
                initial['project']=latest.project

            except Expense.DoesNotExist:
                pass

            form = ExpenseForm(initial=initial)

        return render(request, 'home.html', {'expenses': expenses,
                                             'form':form})

    return render(request, 'home.html', {})

def mobile_login(request):
    username = request.REQUEST.get('u', False)
    password = request.REQUEST.get('p', False)

    try:
        user = User.objects.get(username=username)
    except:
        user = None

    if not (user and check_password(password, user.password)):
        raise Http404('Invalid username or password supplied.')

    organisation = Organisation.objects.get(pk=1)
    auth_token = AuthToken.objects.create(user=user)

    return HttpResponse(get_sync_data(auth_token), mimetype='text/plain')

def add_expense(request):
    q = request.GET.get('q', '')
    exp_qs = q.split('|')
    print exp_qs

    expenses = []
    for exp_q in exp_qs:
        exp_q = exp_q.split(',')

        exp_dict = {
                'token':get_object_or_404(AuthToken, key=exp_q[0]),
                'location':get_by_title(Location, exp_q[1]),
                'amount': float(exp_q[2]),
                'type':exp_q[3],
                'project':get_by_title(Project, exp_q[4]),
                'category':get_by_title(Category, exp_q[5]),
                'billed': True if exp_q[6] else False,
                'bill_id': exp_q[6],
                'time': datetime.fromtimestamp(float(exp_q[7])),
                }
        expense = Expense.objects.create(**exp_dict)
        expenses.append(expense)
        response = create_csv(len(expenses))

    return HttpResponse(response, mimetype='text/plain')

def sync(request):
    auth_token = get_object_or_404(AuthToken,
                                   key=request.REQUEST.get('token', False))
    return HttpResponse(get_sync_data(auth_token), mimetype='text/plain')



