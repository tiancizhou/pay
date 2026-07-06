const FALLBACK_LOCATION = {
  latitude: 29.3069,
  longitude: 120.0751,
  label: '浙江省金华市义乌市世俊路附近'
}

export function locate(label = '当前位置') {
  if (!navigator.geolocation) {
    return Promise.resolve({ ...FALLBACK_LOCATION, label: FALLBACK_LOCATION.label })
  }

  return new Promise((resolve) => {
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const point = {
          latitude: Number(position.coords.latitude.toFixed(6)),
          longitude: Number(position.coords.longitude.toFixed(6)),
          label
        }
        resolve(await withAddressLabel(point))
      },
      () => resolve({ ...FALLBACK_LOCATION, label: FALLBACK_LOCATION.label }),
      { enableHighAccuracy: true, timeout: 5000, maximumAge: 60000 }
    )
  })
}

async function withAddressLabel(point) {
  try {
    const url = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&accept-language=zh-CN&lat=${point.latitude}&lon=${point.longitude}`
    const controller = new AbortController()
    const timer = window.setTimeout(() => controller.abort(), 2500)
    const response = await fetch(url, { signal: controller.signal })
    window.clearTimeout(timer)
    if (!response.ok) return point
    const data = await response.json()
    return {
      ...point,
      label: formatAddress(data) || point.label
    }
  } catch {
    return { ...point, label: FALLBACK_LOCATION.label }
  }
}

function formatAddress(data) {
  const address = data?.address ?? {}
  const parts = [
    address.state,
    address.city || address.town || address.county,
    address.suburb || address.city_district || address.district,
    address.road || address.neighbourhood,
    address.house_number
  ].filter(Boolean)
  return parts.length ? [...new Set(parts)].join('') : data?.display_name
}

export function cityFromRegion(region) {
  const match = region?.match(/([^省自治区]+市)/)
  return match?.[1] ?? '当前位置'
}
