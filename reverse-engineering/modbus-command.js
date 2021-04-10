class ModbusCommand {
  constructor () {
    this.CRC16H = 0
    this.CRC16L = 0

    this.address = {
      coil: 4097,
      disk: 8193,
      hold: 12289,
      input: 16385
    }

    /*
    this.client_buffer_size = 7
    this.client_coil_size = 47
    this.client_disk_size = 5
    this.client_hold_size = 52
    this.client_input_size = 31
    this.comm_max_failed_times = 4
    this.delay_time_setup = 10
    this.error_addr = 3
    this.error_crc = 1
    this.error_func = 2
    this.error_operate = 5
    this.error_time_out = 6
    this.error_value = 4
    this.size_coil = 20
    this.size_disk = 20
    this.size_hold = 80
    this.size_input = 20
    this.success = 0
    this.time_out_setup = 3000
    this.net_connect_failed = 11
    this.net_connect_success = 10
    this.net_connect_tcpclose = 12
    this.ip = null
    this.commFaiedTimes = 0
    this.canWrite = true
    this.flag = false
    */

    this.valueCoil = new Array(20).fill(0)
    this.valueDisc = new Array(20).fill(0)
    this.valueHold = new Array(80).fill(0)
    this.valueInput = new Array(20).fill(0)
    this.list = []
  }

  /*
  getIp() {
    return this.ip
  }

  setIp(ip) {
    this.ip = ip
  }

  addCommFaiedTimes () {
    this.commFaiedTimes++
  }

  clearCommFaiedTimes () {
    this.commFaiedTimes = 0
  }

  isCommFailed () {
    return this.commFaiedTimes === 4
  }
  */

  addCommand (cmd) {
    // this.canWrite = false
    this.list.push(cmd)
  }

  clearCommand () {
    this.list = []
  }

  isEmpty () {
    return this.list.length < 1
  }

  removeFirst () {
    this.list.shift()
  }

  CRC16 (bufData, buflen) {
    let CRC = 65535
    if (buflen !== 0) {
      for (let i = 0; i < buflen; i++) {
        CRC ^= bufData[i] & 255
        for (let j = 0; j < 8; j++) {
          if ((CRC & 1) !== 0) {
            CRC = (CRC >> 1) ^ 40961
          } else {
            CRC >>= 1
          }
        }
      }
      this.CRC16L = ((CRC & 255) | 0)
      this.CRC16H = ((CRC >> 8) | 0)
    }
    return 0
  }
}

module.exports = new ModbusCommand()
