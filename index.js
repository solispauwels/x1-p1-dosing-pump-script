const pump = require('./pump')

const dosing = async () => {
  await pump.start()
  await pump.sleep()
  await pump.stop()
}

dosing()
