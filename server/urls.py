from django.conf.urls.defaults import *
from django.views.generic.simple import direct_to_template
from django.contrib import admin
from django.contrib.auth.views import login, logout, password_change, password_change_done
from django.contrib.auth.decorators import login_required

admin.autodiscover()

urlpatterns = patterns('',
    (r'^', include('main.urls')),
    url(r'^accounts/login/$', login, name="login"),
    url(r'^accounts/logout/$', logout, name="logout"),
    url(r'^accounts/changepass/$', password_change, name="changepass"),
    url(r'^accounts/changepass-done/$', password_change_done,
        name="changepass-done"),
    url(r'^account/$', login_required(direct_to_template), {'template': 'registration/account.html'},
        name='account'),
    (r'^admin/', include(admin.site.urls)),
)
