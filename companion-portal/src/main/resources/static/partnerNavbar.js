async function loadPartnerNavbar() {
	const host = document.getElementById("navbar");
	if (!host) return;

	const navRes = await fetch("/portal/navbar.html");
	if (!navRes.ok) return;

	host.innerHTML = await navRes.text();

	const right = document.getElementById("partnerNavbarRight");
	if (!right) return;

	try {
		const res = await fetch("/portal/partner/bookings/data");

		if (res.status === 200) {
			right.innerHTML = `
		    <a class="btn btn-soft btn-view" href="/portal/partner/bookings">Partner Bookings</a>
		    <a class="btn btn-soft btn-logout" href="/portal/partner/logout">Logout</a>
		  `;
		} else {
			right.innerHTML = `
			    <a class="btn btn-soft btn-logout" href="/portal/partner/login">Login</a>
			  `;
		}
	} catch (e) {
		right.innerHTML = `
      <a class="btn btn-dark" href="/portal/partner/login">
        Login
      </a>
    `;
	}
}

document.addEventListener("DOMContentLoaded", loadPartnerNavbar);
