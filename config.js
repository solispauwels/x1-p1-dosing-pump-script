module.exports = {
  mac: '00:00:00:00:00:00', // the bluetooth MAC address
  handler: '0x0036', // The bluetooth characteristic handler
  volume: 5, // liquid volume in ml
  calibration: 1.02, // How many ml are really filled when you set 1ml (it is easier to mesure with amounts bigger then 5ml)
  speed: 1.6 // The time it takes in seconds for a ml to get filled
}
