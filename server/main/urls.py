from django.conf.urls.defaults import patterns, url
import django.views.generic as gen_views
from main.models import *

urlpatterns = patterns('main.views',
    url(r'^$', 'home', name='home'),
    url(r'^mobile-login/', 'mobile_login', name='mobile-login'),
    url(r'^add-expense/', 'add_expense', name='add-expense'),
    url(r'^sync/', 'sync', name='sync'),
)
