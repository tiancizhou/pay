import { reactive } from 'vue'

export const booking = reactive({
  service: null,
  technician: null,
  address: {
    contactName: '',
    phone: '',
    region: '',
    detail: '',
    defaultAddress: true
  },
  addresses: [],
  customerLocation: null,
  latestOrder: null,
  serviceTime: '点击选择服务时间'
})

export function upsertAddress(address) {
  const id = address.id ?? `addr-${Date.now()}`
  const next = { ...address, id }
  if (next.defaultAddress) {
    booking.addresses.forEach((item) => {
      item.defaultAddress = false
    })
  }
  const index = booking.addresses.findIndex((item) => item.id === id)
  if (index >= 0) {
    booking.addresses[index] = next
  } else {
    booking.addresses.push(next)
  }
  if (next.defaultAddress || !booking.address.contactName) {
    booking.address = { ...next }
  }
  return next
}

export function selectAddress(address) {
  booking.address = { ...address }
}
