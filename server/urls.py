from django.conf.urls.defaults import *
from django.contrib import admin
from django.contrib.auth.views import login, logout, password_change, password_change_done
from django.contrib.auth.decorators import login_required

admin.autodiscover()

urlpatterns = patterns('',
    (r'^', include('main.urls')),
    (r'^accounts/', include('accounts.urls')),
    (r'^admin/', include(admin.site.urls)),
)
