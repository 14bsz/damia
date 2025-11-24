import request from '@/utils/request'

export function getSeatRelateInfo(data) {
  return request({
    url: '/damai/program/seat/relate/info',
    method: 'post',
    data: data
  })
}