let maxDate = new Date();
maxDate = maxDate.setMonth(maxDate.getMonth() + 3);

flatpickr('#fromCheckinDateTime', {
 enableTime: true,
 dateFormat: "Y-m-d H:i",
 locale: 'ja',
 minDate: 'today',
 maxDate: maxDate
});