const command = require('./modbus-command')
const readWrite = require('./read-write')

command.clearCommand()

const manualVolume = 1.0

// start

command.valueHold[50] = manualVolume * 10
command.valueHold[49] = 0

command.valueCoil[2] = 1
command.addCommand('1 6 50')
command.addCommand('1 6 49')
command.addCommand('1 5 2')

console.log(readWrite.getbytes())

// stop

// case 1

command.clearCommand()

command.valueCoil[1] = 1
command.valueHold[49] = 0
command.addCommand('1 6 49')
command.addCommand('1 5 1')

console.log(readWrite.getbytes())

// case 2

command.clearCommand()

command.valueCoil[1] = 0
command.addCommand('1 5 1')
console.log(readWrite.getbytes())
