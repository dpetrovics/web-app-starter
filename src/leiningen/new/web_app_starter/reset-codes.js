//make POST request to reset the reset pw code, send out new email.
function resetPwCode (username) {
	var form = document.createElement('form');
	form.setAttribute('method', 'post');
	form.setAttribute('action', "/reset-pw-code/" + username);
	form.style.display = 'hidden';
	document.body.appendChild(form)
	form.submit();
}

//make POST request to reset the activation code, send out new email.
function resetActivation (username) {
	var form = document.createElement('form');
	form.setAttribute('method', 'post');
	form.setAttribute('action', "/reset-activation/" + username);
	form.style.display = 'hidden';
	document.body.appendChild(form)
	form.submit();
}
