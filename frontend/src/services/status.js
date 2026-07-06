export const statusText = {
  WAITING_PAYMENT: '待支付',
  PAID_TO_MERCHANT: '已支付到商户',
  WAITING_TECHNICIAN_ACCEPT: '待技师接单',
  TECHNICIAN_ACCEPTED: '技师已接单',
  CONTACTED_CUSTOMER: '已联系客户',
  ON_THE_WAY: '技师上门中',
  ARRIVED: '技师已到达',
  CLOCKED_IN: '服务上钟中',
  CLOCKED_OUT: '服务已下钟',
  FINISHED: '已完成',
  REFUND_REQUESTED: '退款中',
  REFUNDED: '已退款',
  CANCELLED: '已取消'
}

export const orderTabs = [
  { key: 'ALL', label: '全部', statuses: [] },
  { key: 'WAITING_ACCEPT', label: '待接单', statuses: ['WAITING_TECHNICIAN_ACCEPT', 'PAID_TO_MERCHANT'] },
  { key: 'SERVING', label: '待服务', statuses: ['TECHNICIAN_ACCEPTED', 'CONTACTED_CUSTOMER', 'ON_THE_WAY', 'ARRIVED', 'CLOCKED_IN', 'CLOCKED_OUT'] },
  { key: 'FINISHED', label: '已完成', statuses: ['FINISHED'] },
  { key: 'REFUND', label: '退款', statuses: ['REFUND_REQUESTED', 'REFUNDED', 'CANCELLED'] }
]
