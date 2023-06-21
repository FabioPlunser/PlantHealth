export function setDates(event: any) {
  let cookieFrom = event.cookies.get("from") || "";
  let cookieTo = event.cookies.get("to") || "";

  let from: Date = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
  let to: Date = new Date(Date.now());
  to.setHours(23, 59, 59, 999);

  if (cookieFrom !== "" && cookieTo !== "") {
    from = new Date(cookieFrom);
    to = new Date(cookieTo);
  }

  let dates = {
    from: from,
    to: to,
  };

  return dates;
}
