const command = require('./modbus-command')

class ReadWrite {
  constructor () {
    this.buffer = null
    this.funcCode = 0
    this.raddr = 0
    this.rcount = 0
    this.status = 0
  }

  getbytes () {
    this.buffer = new Array(20).fill(0)

    if (command.list.length < 1) {
      return null
    }

    const commands = command.list[0].split(' ')

    commands.forEach((item, index) => { this.buffer[index] = parseInt(item) || 0 })

    this.funcCode = parseInt(commands[1])
    this.raddr = parseInt(commands[2])

    if (commands.length > 3) {
      this.rcount = parseInt(commands[3])
    }

    if (this.funcCode === 1) {
      this.modbus01((parseInt(commands[0]) | 0), this.raddr, parseInt(commands[3]))
    } else if (this.funcCode === 2) {
      this.modbus02((parseInt(commands[0]) | 0), this.raddr, parseInt(commands[3]))
    } else if (this.funcCode === 3) {
      this.modbus03((parseInt(commands[0]) | 0), this.raddr, parseInt(commands[3]))
    } else if (this.funcCode === 4) {
      this.modbus04((parseInt(commands[0]) | 0), this.raddr, parseInt(commands[3]))
    } else if (this.funcCode === 5) {
      this.modbus05((parseInt(commands[0]) | 0), this.raddr)
    } else if (this.funcCode === 6) {
      this.modbus06((parseInt(commands[0]) | 0), this.raddr)
    } else if (this.funcCode === 15) {
      this.modbus0F((parseInt(commands[0]) | 0), this.raddr, parseInt(commands[3]))
    } else if (this.funcCode === 16) {
      this.modbus10((parseInt(commands[0]) | 0), this.raddr, parseInt(commands[3]))
    }

    return this.buffer
  }

  modbus01 (addr, startAddress, count) {
    const address = startAddress + command.address.coil
    try {
      this.buffer[0] = addr
      this.buffer[1] = 1
      this.buffer[2] = ((address >> 8) | 0)
      this.buffer[3] = ((address & 255) | 0)
      this.buffer[4] = ((count >> 8) | 0)
      this.buffer[5] = ((count & 255) | 0)
      command.CRC16(this.buffer, 6)
      this.buffer[6] = command.CRC16L
      this.buffer[7] = command.CRC16H
    } catch (e) {
      console.error(e.message, e)
    }
  }

  modbus02 (addr, startAddress, count) {
    const address = startAddress + command.address.disk
    try {
      this.buffer[0] = addr
      this.buffer[1] = 2
      this.buffer[2] = ((address >> 8) | 0)
      this.buffer[3] = ((address & 255) | 0)
      this.buffer[4] = ((count >> 8) | 0)
      this.buffer[5] = ((count & 255) | 0)
      command.CRC16(this.buffer, 6)
      this.buffer[6] = command.CRC16L
      this.buffer[7] = command.CRC16H
    } catch (e) {
      console.error(e.message, e)
    }
  }

  modbus03 (addr, startAddress, count) {
    const address = startAddress + command.address.hold
    try {
      this.buffer[0] = addr
      this.buffer[1] = 3
      this.buffer[2] = ((address >> 8) | 0)
      this.buffer[3] = ((address & 255) | 0)
      this.buffer[4] = ((count >> 8) | 0)
      this.buffer[5] = ((count & 255) | 0)
      command.CRC16(this.buffer, 6)
      this.buffer[6] = command.CRC16L
      this.buffer[7] = command.CRC16H
    } catch (e) {
      console.error(e.message, e)
    }
  }

  modbus04 (addr, startAddress, count) {
    const address = startAddress + command.address.input
    try {
      this.buffer[0] = addr
      this.buffer[1] = 4
      this.buffer[2] = ((address >> 8) | 0)
      this.buffer[3] = ((address & 255) | 0)
      this.buffer[4] = ((count >> 8) | 0)
      this.buffer[5] = ((count & 255) | 0)
      command.CRC16(this.buffer, 6)
      this.buffer[6] = command.CRC16L
      this.buffer[7] = command.CRC16H
    } catch (e) {
      console.error(e.message, e)
    }
  }

  modbus05 (addr, startAddress) {
    let value = 0
    const address = startAddress + command.address.coil
    try {
      if (command.valueCoil[startAddress] === 1) {
        value = 65280
      }
      this.buffer[0] = addr
      this.buffer[1] = 5
      this.buffer[2] = ((address >> 8) | 0)
      this.buffer[3] = ((address & 255) | 0)
      this.buffer[4] = ((value >> 8) | 0)
      this.buffer[5] = ((value & 255) | 0)
      command.CRC16(this.buffer, 6)
      this.buffer[6] = command.CRC16L
      this.buffer[7] = command.CRC16H
    } catch (e) {
      console.error(e.message, e)
    }
  }

  modbus06 (addr, startAddress) {
    const address = startAddress + command.address.hold
    try {
      this.buffer[0] = addr
      this.buffer[1] = 6
      this.buffer[2] = ((address >> 8) | 0)
      this.buffer[3] = ((address & 255) | 0)
      this.buffer[4] = ((command.valueHold[startAddress] >> 8) | 0)
      this.buffer[5] = ((command.valueHold[startAddress] & 255) | 0)
      command.CRC16(this.buffer, 6)
      this.buffer[6] = command.CRC16L
      this.buffer[7] = command.CRC16H
    } catch (e) {
      console.error(e.message, e)
    }
  }

  modbus10 (addr, startAddress, regCount) {
    const address = startAddress + command.address.hold
    const byteCount = regCount * 2
    try {
      this.buffer[0] = addr
      this.buffer[1] = 16
      this.buffer[2] = ((address >> 8) | 0)
      this.buffer[3] = ((address & 255) | 0)
      this.buffer[4] = ((regCount >> 8) | 0)
      this.buffer[5] = ((regCount & 255) | 0)
      this.buffer[6] = ((regCount * 2) | 0)
      let index = 0
      for (let i = 0; i < byteCount; i += 2) {
        const data = command.valueHold[startAddress + index]
        this.buffer[i + 7] = ((data >> 8) | 0)
        this.buffer[i + 8] = ((data & 255) | 0)
        index++
      }
      command.CRC16(this.buffer, byteCount + 7)
      this.buffer[byteCount + 7] = command.CRC16L
      this.buffer[byteCount + 8] = command.CRC16H
      /*
      let str = ''

      for (let i2 = 0; i2 < 7; i2++) {
        str += this.buffer[i2].toString(16)
      }
      */
    } catch (e) {
      console.error(e.message, e)
    }
  }

  modbus0F (addr, startAddress, regCount) {
    let byteCount
    const address = startAddress + command.address.coil
    try {
      if (regCount % 8 === 0) {
        byteCount = (regCount / 8 | 0)
      } else {
        byteCount = ((regCount / 8 | 0)) + 1
      }
      this.buffer[0] = addr
      this.buffer[1] = 15
      this.buffer[2] = ((address >> 8) | 0)
      this.buffer[3] = ((address & 255) | 0)
      this.buffer[4] = (((regCount >> 8) & 255) | 0)
      this.buffer[5] = ((regCount & 255) | 0)
      this.buffer[6] = (byteCount | 0)
      let data = 0
      let index = 0
      let byteIndex = 0
      for (let i = 0; i < regCount; i++) {
        if (command.valueCoil[startAddress + i] === 1) {
          data |= 1 << index
        }
        index++
        if (index === 8) {
          index = 0
          this.buffer[byteIndex + 7] = (data | 0)
          byteIndex++
        }
      }
      if (index > 0) {
        this.buffer[byteIndex + 7] = (data | 0)
      }
      command.CRC16(this.buffer, byteCount + 7)
      this.buffer[byteCount + 7] = command.CRC16L
      this.buffer[byteCount + 8] = command.CRC16H
      // let i2 = byteCount + 9
    } catch (e) {
      console.error(e.message, e)
    }
  }

  saveData (buffer2) {
    const length = buffer2.length
    if (length > 0) {
      command.CRC16(buffer2, length - 2)
      if (buffer2[length - 2] !== command.CRC16L || buffer2[length - 1] !== command.CRC16H) {
        this.status = 1
        return 3
      } else if (buffer2[1] === (((this.funcCode + 128) | 0))) {
        this.status = 2
        return 4
      } else {
        this.status = 0
        this.modbusDecode(this.raddr, this.rcount, buffer2)
        if (!command.isEmpty()) {
          command.removeFirst()
        }
        if (this.status === 0) {
          if (!command.isEmpty()) {
            return 2
          }
          return 1
        }
      }
    }
    return -1
  }

  modbusDecode (address, rcount2, buffer2) {
    try {
      if (buffer2[1] === 2) {
        let index = 3
        let bitIndex = 0
        let tmpdata = buffer2[3] & 255
        for (let i = 0; i < rcount2; i++) {
          if (((1 << bitIndex) & tmpdata) > 0) {
            command.valueDisc[address + i] = 1
          } else {
            command.valueDisc[address + i] = 0
          }
          bitIndex++
          if (bitIndex === 8) {
            bitIndex = 0
            index++
            tmpdata = buffer2[index] & 255
          }
        }
      } else if (buffer2[1] === 1) {
        let index2 = 3
        let bitIndex2 = 0
        let tmpdata2 = buffer2[3] & 255
        for (let i2 = 0; i2 < rcount2; i2++) {
          if (((1 << bitIndex2) & tmpdata2) > 0) {
            command.valueCoil[address + i2] = 1
          } else {
            command.valueCoil[address + i2] = 0
          }
          bitIndex2++
          if (bitIndex2 === 8) {
            bitIndex2 = 0
            index2++
            tmpdata2 = buffer2[index2] & 255
          }
        }
      } else if (buffer2[1] === 4) {
        let index3 = 0
        const count = rcount2 * 2
        for (let i3 = 0; i3 < count; i3 += 2) {
          command.valueInput[address + index3] = ((buffer2[i3 + 3] & 255) << 8) + (buffer2[i3 + 4] & 255)
          index3++
        }
      } else if (buffer2[1] === 3) {
        let index4 = 0
        const count2 = rcount2 * 2
        for (let i4 = 0; i4 < count2; i4 += 2) {
          command.valueHold[address + index4] = ((buffer2[i4 + 3] & 255) << 8) + (buffer2[i4 + 4] & 255)
          index4++
        }
      }
    } catch (e) {
      console.error(e.message, e)
    }
  }
}

module.exports = new ReadWrite()
