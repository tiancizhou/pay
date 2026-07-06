const FALLBACK_LOCATION = {
  latitude: 31.701,
  longitude: 119.943,
  label: '江苏省常州市武进区'
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
    return point
  }
}

function formatAddress(data) {
  const address = data?.address ?? {}
  const district = firstPresent(
    address.county,
    address.city_district,
    address.district
  )
  const area = firstPresent(
    address.suburb,
    address.neighbourhood,
    address.quarter
  )
  const city = firstPresent(
    address.city,
    address.town,
    cityFromDevelopmentZone(area, district),
    cityFromDevelopmentZone(district, district)
  )
  const parts = [
    address.state,
    city,
    district,
    area,
    address.road,
    address.house_number
  ].filter(Boolean)
  const formatted = parts.length ? uniqueParts(parts).join('') : data?.display_name
  return normalizeRegionLabel(formatted)
}

export function cityFromRegion(region) {
  return locationPartsFromRegion(region).city
}

export function normalizeRegionLabel(region) {
  const value = region?.trim()
  if (!value) return ''
  if (directCityFromRegion(value)) return value

  const district = parseDistrict(value)
  const city = cityFromDevelopmentZone(value, district)
  if (!city) return value

  const state = value.match(/^(.+?(?:省|自治区|特别行政区))/)?.[1]
  if (!state) return `${city}${value}`
  return `${state}${city}${value.slice(state.length)}`
}

export function locationPartsFromRegion(region) {
  const value = region?.trim()
  if (!value) return { city: '当前位置', district: '请定位' }

  const district = parseDistrict(value)
  const city = parseCity(value, district)
  return {
    city: city || district || '当前位置',
    district: district && district !== city ? district : '附近'
  }
}

function parseCity(value, district = '') {
  const directCity = directCityFromRegion(value)
  if (directCity) return directCity

  const developmentZoneCity = cityFromDevelopmentZone(value, district)
  if (developmentZoneCity) return developmentZoneCity

  return ''
}

function parseDistrict(value) {
  const directDistrict = value.match(/([^省市区县]+(?:区|县|旗))/)
  return directDistrict?.[1] ?? ''
}

function directCityFromRegion(value = '') {
  const match = value.match(/([^省自治区市]+市)/)
  return match?.[1] ?? ''
}

function cityFromDevelopmentZone(value = '', district = '') {
  const match = value.match(/([^省市区县]+?)(?:经济技术开发区|经济开发区|高新技术产业开发区|高新区|开发区)/)
  if (!match) return ''
  const name = match[1].replace(/^(.*?省)?(.*?区)?/, '')
  if (district && district.startsWith(name)) return ''
  return name ? `${name}市` : ''
}

function firstPresent(...values) {
  return values.find((value) => Boolean(value)) ?? ''
}

function uniqueParts(parts) {
  const seen = new Set()
  return parts.filter((part) => {
    if (seen.has(part)) return false
    seen.add(part)
    return true
  })
}
