const { exec } = require('child_process')

const config = require('./config')
const { volume = 5, calibration = 1, mac, handler, speed } = config

class Pump {
  constructor () {
    this.timeout = 0
  }

  setVolume (manualVolume = volume, manualCalibration = calibration) {
    const volume = Math.floor((manualVolume / calibration) * 10)
    const buffer = [1, 6, 48, 51, ((volume >> 8) | 0), ((volume & 255) | 0)]

    this.timeout = Math.ceil(speed * manualVolume)

    return this.execute(this.getCommand(this.getBuffer(buffer)))
  }

  sleep () {
    return this.execute(`sleep ${this.timeout + 1}`)
  }

  async start (manualVolume, manualCalibration) {
    await this.setVolume(manualVolume, manualCalibration)
    return this.execute(this.getCommand([1, 5, 16, 2, 255, 0, 41, 58]))
  }

  stop () {
    return this.execute(this.getCommand([1, 5, 16, 2, 0, 0, 104, 202]))
  }

  getCommand (buffer) {
    return `gatttool -b ${mac} --char-write-req -a ${handler} -n ${buffer.map(item => item.toString(16).padStart(2, '0')).join('')}`
  }

  execute (command) {
    return this.executePromise(command).catch(error => console.error(error) && error).then(stdout => stdout && console.log(stdout))
  }

  executePromise (command) {
    return new Promise((resolve, reject) => exec(command, (error, stdout, stderr) => error || stderr ? reject(error || stderr) : resolve(stdout)))
  }

  getBuffer (buffer) {
    let crc = 65535
    buffer.forEach(item => {
      crc ^= item & 255
      ;[...new Array(8)].forEach(() => {
        if ((crc & 1) !== 0) {
          crc = (crc >> 1) ^ 40961
        } else {
          crc >>= 1
        }
      })
    })
    return [...buffer, ((crc & 255) | 0), ((crc >> 8) | 0)]
  }
}

module.exports = new Pump()
