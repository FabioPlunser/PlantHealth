/**
 * The function determines the type of device (desktop, tablet, or mobile) based on the user agent
 * string.
 * @returns a string indicating the type of device based on the user agent string. It returns "tablet"
 * if the user agent string matches a tablet or iPad device, "mobile" if it matches a mobile device,
 * and "desktop" if it matches a desktop device.
 */
export function getDeviceType() {
  const ua = navigator.userAgent;
  if (/(tablet|ipad|playbook|silk)|(android(?!.*mobi))/i.test(ua)) {
    return "tablet";
  }
  if (
    /Mobile|iP(hone|od)|Android|BlackBerry|IEMobile|Kindle|Silk-Accelerated|(hpw|web)OS|Opera M(obi|ini)/.test(
      ua
    )
  ) {
    return "mobile";
  }
  return "desktop";
}
