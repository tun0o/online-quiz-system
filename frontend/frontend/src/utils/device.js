export function getDeviceName() {
    const ua = navigator.userAgent || '';
    if (/iPhone/i.test(ua)) return 'iPhone';
    if (/iPad/i.test(ua)) return 'iPad';
    if (/Android/i.test(ua)) return 'Android Device';
    if (/Windows/i.test(ua)) return 'Windows PC';
    if (/Macintosh/i.test(ua)) return 'Mac';
    return 'Browser Device';
}

export async function getDeviceFingerprint() {
    const ua = navigator.userAgent || '';
    const lang = navigator.language || '';
    const tz = Intl.DateTimeFormat().resolvedOptions().timeZone || '';
    const res = `${ua}|${lang}|${tz}|${screen.width}x${screen.height}`;
    const enc = new TextEncoder();
    const data = enc.encode(res);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

export async function ensureDeviceIdentity() {
    if (!localStorage.getItem('deviceName')) {
        localStorage.setItem('deviceName', getDeviceName());
    }
    if (!localStorage.getItem('deviceFingerprint')) {
        const fp = await getDeviceFingerprint();
        localStorage.setItem('deviceFingerprint', fp);
    }
}


