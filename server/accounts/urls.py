from django.conf.urls.defaults import patterns, url
from django.contrib.auth.views import login, logout, password_change, password_change_done
from django.contrib.auth.decorators import login_required
from django.views.generic.simple import direct_to_template

urlpatterns = patterns('',
    url(r'^login/$', login, name="login"),
    url(r'^logout/$', logout, name="logout"),
    url(r'^changepass/$', password_change, name="changepass"),
    url(r'^changepass-done/$', password_change_done,
        name="changepass-done"),
    url(r'^details/$', login_required(direct_to_template), {'template': 'registration/account.html'},
        name='account'),
    url(r'^edit/$', 'accounts.views.edit_profile', name = 'edit-profile')
)
