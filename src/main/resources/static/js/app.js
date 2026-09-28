const api = '/api';
const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => [...document.querySelectorAll(selector)];
let slots = [];
let bookings = [];
let lots = [];
let toastTimer;

function showToast(message, error = false) {
  const toast = $('#toast');
  toast.textContent = message;
  toast.className = `toast show${error ? ' error' : ''}`;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.className = 'toast', 3600);
}

async function request(path, options = {}) {
  const response = await fetch(api + path, {headers: {'Content-Type': 'application/json'}, ...options});
  if (response.ok) return response.status === 204 ? null : response.json();
  let message = 'The request could not be completed.';
  try { message = (await response.json()).message || message; } catch (_) {}
  if (response.status === 400) message = 'Please check the entered values.';
  if (response.status === 404) message = 'The requested parking resource was not found.';
  if (response.status === 409) message = 'Slot unavailable for the selected time window.';
  if (response.status >= 500) message = 'The parking service is temporarily unavailable. Please try again.';
  throw new Error(message);
}

function format(value) {
  return value ? new Date(value).toLocaleString([], {dateStyle: 'medium', timeStyle: 'short'}) : '-';
}

function statusInfo(status) {
  const statuses = {
    AVAILABLE: ['AVAILABLE', 'green'],
    SELECTED: ['SELECTED', 'blue'],
    BOOKED: ['CONFIRMED', 'green'],
    CHECKED_IN: ['ACTIVE', 'blue'],
    OCCUPIED: ['OCCUPIED', 'red'],
    RESERVED: ['RESERVED', 'orange'],
    COMPLETED: ['COMPLETED', 'gray'],
    CANCELLED: ['CANCELLED', 'red'],
    OVERSTAY: ['OVERSTAY', 'orange'],
    MAINTENANCE: ['MAINTENANCE', 'gray']
  };
  return statuses[status] || [String(status || 'UNKNOWN').replaceAll('_', ' '), 'gray'];
}

function badge(status) {
  const [label, kind] = statusInfo(status);
  return `<span class="badge ${kind}">${label}</span>`;
}

function statusClass(status) {
  if (status === 'AVAILABLE') return 'status-available';
  if (status === 'OCCUPIED' || status === 'CHECKED_IN') return 'status-occupied';
  if (status === 'RESERVED' || status === 'BOOKED') return 'status-reserved';
  return '';
}

function navigate() {
  const requestedHash = location.hash || '#dashboard';
  const currentHash = $(`.view[id="${requestedHash.slice(1)}"]`) ? requestedHash : '#dashboard';
  $$('.view').forEach(view => view.classList.toggle('active', `#${view.id}` === currentHash));
  $$('.topbar nav a').forEach(link => link.classList.toggle('active', link.hash === currentHash));
  if (currentHash === '#bookings') renderBookings();
}

function renderLots() {
  const list = $('#lots-list');
  list.innerHTML = lots.length ? lots.map(lot => {
    const count = lot.totalSlots ?? slots.filter(slot => slot.parkingLotId === lot.id).length;
    return `<article class="lot-card"><p class="eyebrow">PARKING LOCATION</p><h2>${lot.name}</h2><p>${lot.location}</p><div class="lot-meta"><span>Total spaces</span><strong>${count}</strong></div><a class="button button-outline" href="#availability" data-lot="${lot.id}">View availability <span aria-hidden="true">→</span></a></article>`;
  }).join('') : '<p class="empty">No parking lots configured.</p>';
  list.querySelectorAll('[data-lot]').forEach(link => link.addEventListener('click', () => {
    $('#booking-lot').value = link.dataset.lot;
    renderBookingSlots();
  }));
}

function renderBookingSlots() {
  const lotId = $('#booking-lot').value;
  const selectedSlot = $('#booking-slot').value;
  const availableSlots = slots.filter(slot => slot.status !== 'MAINTENANCE' && (!lotId || String(slot.parkingLotId) === lotId));
  $('#booking-slot').innerHTML = '<option value="">Choose a slot</option>' + availableSlots.map(slot => `<option value="${slot.id}">${slot.slotNumber} · ${slot.parkingLotName}</option>`).join('');
  if (availableSlots.some(slot => String(slot.id) === selectedSlot)) $('#booking-slot').value = selectedSlot;
  renderBookingSelection();
}

function renderBookingSelection() {
  const selected = slots.find(slot => String(slot.id) === $('#booking-slot').value);
  $('#booking-selection').textContent = selected ? `${selected.slotNumber} · ${selected.parkingLotName} · ${selected.location}` : 'Select a lot and slot to review your space.';
}

async function loadData() {
  try {
    [slots, bookings, lots] = await Promise.all([request('/slots'), request('/bookings'), request('/parking-lots')]);
    $('#lot-count').textContent = lots.length;
    $('#slot-count').textContent = slots.length;
    $('#available-count').textContent = slots.filter(slot => slot.status === 'AVAILABLE').length;
    $('#active-count').textContent = bookings.filter(booking => ['BOOKED', 'CHECKED_IN'].includes(booking.status)).length;
    $('#occupancy-list').innerHTML = slots.slice(0, 10).map(slot => `<div class="occupancy ${statusClass(slot.status)}"><b>${slot.slotNumber}</b><small>${String(slot.status).replaceAll('_', ' ')}</small></div>`).join('') || '<p class="empty">No slots configured.</p>';
    $('#booking-lot').innerHTML = '<option value="">Choose a parking lot</option>' + lots.map(lot => `<option value="${lot.id}">${lot.name} · ${lot.location}</option>`).join('');
    renderBookingSlots();
    renderLots();
    renderBookings();
  } catch (error) {
    showToast(error.message, true);
  }
}

function renderBookings() {
  const body = $('#bookings-body');
  body.innerHTML = bookings.length ? bookings.map(booking => `<tr>
    <td>#${booking.id}</td><td>${booking.userName}</td><td>${booking.vehicleNumber}</td><td>${booking.slotNumber}</td>
    <td>${format(booking.startTime)}<br>${format(booking.endTime)}</td><td>${badge(booking.status)}</td>
    <td><div class="table-actions">
      ${booking.status === 'BOOKED' ? `<button class="button mini" data-action="checkin" data-id="${booking.id}">Check In</button>` : ''}
      ${booking.status === 'CHECKED_IN' ? `<button class="button mini button-warn" data-action="checkout" data-id="${booking.id}">Check Out</button>` : ''}
      ${['BOOKED', 'CHECKED_IN'].includes(booking.status) ? `<button class="button mini button-outline" data-action="cancel" data-id="${booking.id}">Cancel</button>` : ''}
    </div></td>
  </tr>`).join('') : '<tr><td colspan="7" class="empty">No bookings yet.</td></tr>';
}

function localDateTime(date, time) {
  const value = new Date(date);
  const duration = Math.max(0, value.getTime() - new Date(time).getTime());
  const hours = Math.floor(duration / 3600000);
  const minutes = Math.floor((duration % 3600000) / 60000);
  return hours ? `${hours} hr ${minutes} min` : `${minutes} min`;
}

function showCheckIn(result) {
  $('#checkout-result').innerHTML = `<div class="receipt">
    <div><span>Booking ID</span><strong>#${result.bookingId}</strong></div>
    <div><span>Vehicle number</span><strong>${result.vehicleNumber}</strong></div>
    <div><span>Check-in time</span><strong>${format(result.checkInTime)}</strong></div>
    <div><span>Parking duration so far</span><strong>${localDateTime(new Date(), result.checkInTime)}</strong></div>
    <div><span>Status</span><strong>${badge(result.status)}</strong></div>
  </div>`;
}

function showCheckOut(result) {
  const overstay = Number(result.overstayMinutes) > 0;
  const duration = localDateTime(result.checkOutTime, result.checkInTime);
  $('#checkout-result').innerHTML = `<div class="receipt">
    <div><span>Booking ID</span><strong>#${result.bookingId}</strong></div>
    <div><span>Vehicle number</span><strong>${result.vehicleNumber}</strong></div>
    <div><span>Check-in time</span><strong>${format(result.checkInTime)}</strong></div>
    <div><span>Checkout time</span><strong>${format(result.checkOutTime)}</strong></div>
    <div><span>Parking duration</span><strong>${duration}</strong></div>
    <div><span>Overstay duration</span><strong>${result.overstayMinutes || 0} minutes</strong></div>
    <div><span>Booking status</span><strong>${badge(result.status)}</strong></div>
  </div><div class="penalty ${overstay ? '' : 'clear'}"><span>${overstay ? `Overstay penalty · ${result.penaltyRatePerMinute} per minute` : 'No overstay penalty'}</span><strong>₹${Number(result.penaltyAmount || 0).toFixed(2)}</strong></div>`;
}

$('#availability-form').addEventListener('submit', async event => {
  event.preventDefault();
  const form = event.currentTarget;
  const button = form.querySelector('[type="submit"]');
  const originalText = button.textContent;
  const data = new FormData(form);
  const startTime = data.get('startTime');
  const endTime = data.get('endTime');
  if (startTime >= endTime) return showToast('Start time must be before end time.', true);
  button.disabled = true;
  button.textContent = 'Searching...';
  try {
    const available = await request(`/slots/available?startTime=${encodeURIComponent(startTime)}&endTime=${encodeURIComponent(endTime)}`);
    $('#availability-results').innerHTML = available.length ? available.map(slot => `<article class="slot-card" data-slot-card="${slot.id}">
      <h3>${slot.slotNumber}</h3><p>${slot.parkingLotName} · ${slot.location}<br>${slot.slotType}</p>${badge('AVAILABLE')}
      <button class="button" type="button" data-select-slot="${slot.id}">Select space <span aria-hidden="true">→</span></button>
    </article>`).join('') : '<p class="empty">No slots are available for that window.</p>';
    $('#availability-results').querySelectorAll('[data-select-slot]').forEach(selectButton => selectButton.addEventListener('click', () => {
      const selected = available.find(slot => String(slot.id) === selectButton.dataset.selectSlot);
      $('#booking-lot').value = String(selected.parkingLotId);
      renderBookingSlots();
      $('#booking-slot').value = String(selected.id);
      renderBookingSelection();
      $('#booking-date').value = startTime.slice(0, 10);
      $('#booking-form').elements.startTime.value = startTime.slice(11, 16);
      $('#booking-form').elements.endTime.value = endTime.slice(11, 16);
      $$('.slot-card').forEach(card => card.classList.toggle('selected', card.dataset.slotCard === String(selected.id)));
      $('#selected-slot').textContent = `Selected ${selected.slotNumber} at ${selected.parkingLotName}, ${selected.location}. Your booking details are ready.`;
      location.hash = '#book';
    }));
  } catch (error) {
    showToast(error.message, true);
  } finally {
    button.disabled = false;
    button.textContent = originalText;
  }
});

$('#booking-lot').addEventListener('change', renderBookingSlots);
$('#booking-slot').addEventListener('change', renderBookingSelection);
$('#booking-form').addEventListener('submit', async event => {
  event.preventDefault();
  const form = event.currentTarget;
  const button = form.querySelector('[type="submit"]');
  const message = $('#booking-message');
  const values = Object.fromEntries(new FormData(form));
  const startTime = `${values.bookingDate}T${values.startTime}`;
  const endTime = `${values.bookingDate}T${values.endTime}`;
  message.className = 'form-message';
  if (startTime >= endTime) {
    message.textContent = 'End time must be later than start time on the selected date.';
    message.className = 'form-message show error';
    return;
  }
  if (!values.userName.trim() || !values.vehicleNumber.trim()) {
    message.textContent = 'Enter your name and vehicle number to continue.';
    message.className = 'form-message show error';
    return;
  }
  const originalText = button.textContent;
  button.disabled = true;
  button.textContent = 'Submitting booking...';
  try {
    await request('/bookings', {method: 'POST', body: JSON.stringify({userName: values.userName.trim(), vehicleNumber: values.vehicleNumber.trim(), slotId: Number(values.slotId), startTime, endTime})});
    showToast('Your parking booking is confirmed.');
    form.reset();
    message.textContent = '';
    await loadData();
    location.hash = '#bookings';
  } catch (error) {
    message.textContent = error.message;
    message.className = 'form-message show error';
  } finally {
    button.disabled = false;
    button.textContent = originalText;
  }
});

$('#bookings-body').addEventListener('click', async event => {
  const button = event.target.closest('[data-action]');
  if (!button) return;
  const {action, id} = button.dataset;
  button.disabled = true;
  try {
    if (action === 'checkin') {
      const result = await request(`/check-in/${id}`, {method: 'POST'});
      showCheckIn(result);
      location.hash = '#check-in-out';
    } else if (action === 'checkout') {
      const result = await request(`/check-out/${id}`, {method: 'POST'});
      showCheckOut(result);
      location.hash = '#check-in-out';
    } else if (action === 'cancel') {
      await request(`/bookings/${id}`, {method: 'DELETE'});
    }
    await loadData();
    showToast(action === 'checkout' ? 'Checkout completed.' : action === 'checkin' ? 'Check-in completed.' : 'Booking cancelled.');
  } catch (error) {
    button.disabled = false;
    showToast(error.message, true);
  }
});

$('#lifecycle-form').addEventListener('submit', async event => {
  event.preventDefault();
  const form = event.currentTarget;
  const button = event.submitter || form.querySelector('[type="submit"]');
  const action = button.value;
  const bookingId = new FormData(form).get('bookingId');
  const message = $('#lifecycle-message');
  const originalText = button.textContent;
  message.className = 'form-message';
  button.disabled = true;
  button.textContent = action === 'checkin' ? 'Checking in...' : 'Checking out...';
  try {
    const result = await request(`/${action === 'checkin' ? 'check-in' : 'check-out'}/${bookingId}`, {method: 'POST'});
    if (action === 'checkin') showCheckIn(result);
    else showCheckOut(result);
    message.textContent = action === 'checkin' ? 'Vehicle checked in successfully.' : 'Checkout completed successfully.';
    message.className = 'form-message show';
    await loadData();
  } catch (error) {
    message.textContent = error.message;
    message.className = 'form-message show error';
  } finally {
    button.disabled = false;
    button.textContent = originalText;
  }
});

$$('[data-refresh]').forEach(button => button.addEventListener('click', loadData));
window.addEventListener('hashchange', navigate);
$('#booking-date').value = new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 10);
navigate();
loadData();
