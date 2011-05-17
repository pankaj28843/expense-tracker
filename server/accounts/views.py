from django.shortcuts import render, get_object_or_404, redirect
from accounts.forms import UserForm


def edit_profile(request):
    if request.method == 'POST':
        form = UserForm(request.POST, instance=request.user)
        if form.is_valid():
            form.save()
            return redirect('account')
    else:
        form = UserForm(instance=request.user)

    return render(request, 'registration/edit_profile.html', {'form':form})
