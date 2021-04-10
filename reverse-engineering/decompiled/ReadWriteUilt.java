package com.kamoer.x1dosingpump.utils;

import android.util.Log;
import com.kamoer.x1dosingpump.sockets.ModbusCommand;

public class ReadWriteUilt {
    private static byte[] buffer;
    static ModbusCommand command;
    public static int funcCode;
    public static int raddr;
    public static int rcount;
    static ReadWriteUilt readWriteUilt;
    private static int status;

    public static ReadWriteUilt getInstance() {
        if (readWriteUilt == null) {
            readWriteUilt = new ReadWriteUilt();
        }
        return readWriteUilt;
    }

    public static byte[] getbytes(ModbusCommand modbusCommand) {
        command = modbusCommand;
        buffer = new byte[20];
        if (command == null || command.cmdList.isEmpty()) {
            return null;
        }
        String[] cmds = command.cmdList.get(0).split(" ");
        for (int i = 0; i < cmds.length; i++) {
            buffer[i] = (byte) Integer.parseInt(cmds[i]);
            Log.i("ROCK", "cmds:" + cmds[i]);
        }
        funcCode = Integer.parseInt(cmds[1]);
        raddr = Integer.parseInt(cmds[2]);
        if (cmds.length > 3) {
            Log.i("ROCK", "cmds[0]:" + cmds[0] + "cmds[1]:" + cmds[1] + "cmds[2]:" + cmds[2] + ",cmds[3]:" + cmds[3]);
            rcount = Integer.parseInt(cmds[3]);
        }
        Log.i("rock", "raddr:" + raddr + ",rcount:" + rcount);
        if (funcCode == 1) {
            modbus01((byte) Integer.parseInt(cmds[0]), raddr, Integer.parseInt(cmds[3]));
        } else if (funcCode == 2) {
            modbus02((byte) Integer.parseInt(cmds[0]), raddr, Integer.parseInt(cmds[3]));
        } else if (funcCode == 3) {
            modbus03((byte) Integer.parseInt(cmds[0]), raddr, Integer.parseInt(cmds[3]));
        } else if (funcCode == 4) {
            modbus04((byte) Integer.parseInt(cmds[0]), raddr, Integer.parseInt(cmds[3]));
        } else if (funcCode == 5) {
            modbus05((byte) Integer.parseInt(cmds[0]), raddr);
        } else if (funcCode == 6) {
            modbus06((byte) Integer.parseInt(cmds[0]), raddr);
        } else if (funcCode == 15) {
            modbus0F((byte) Integer.parseInt(cmds[0]), raddr, Integer.parseInt(cmds[3]));
        } else if (funcCode == 16) {
            modbus10((byte) Integer.parseInt(cmds[0]), raddr, Integer.parseInt(cmds[3]));
        }
        return buffer;
    }

    public static void modbus01(byte addr, int start_addr, int count) {
        int address = start_addr + ModbusCommand.MODBUS_ADDR_COIL;
        try {
            buffer[0] = addr;
            buffer[1] = 1;
            buffer[2] = (byte) (address >> 8);
            buffer[3] = (byte) (address & 255);
            buffer[4] = (byte) (count >> 8);
            buffer[5] = (byte) (count & 255);
            ModbusCommand.Funct_CRC16(buffer, 6);
            buffer[6] = ModbusCommand.CRC16L;
            buffer[7] = ModbusCommand.CRC16H;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void modbus02(byte addr, int start_addr, int count) {
        int address = start_addr + ModbusCommand.MODBUS_ADDR_DISK;
        try {
            buffer[0] = addr;
            buffer[1] = 2;
            buffer[2] = (byte) (address >> 8);
            buffer[3] = (byte) (address & 255);
            buffer[4] = (byte) (count >> 8);
            buffer[5] = (byte) (count & 255);
            ModbusCommand.Funct_CRC16(buffer, 6);
            buffer[6] = ModbusCommand.CRC16L;
            buffer[7] = ModbusCommand.CRC16H;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void modbus03(byte addr, int start_addr, int count) {
        int address = start_addr + ModbusCommand.MODBUS_ADDR_HOLD;
        try {
            buffer[0] = addr;
            buffer[1] = 3;
            buffer[2] = (byte) (address >> 8);
            buffer[3] = (byte) (address & 255);
            buffer[4] = (byte) (count >> 8);
            buffer[5] = (byte) (count & 255);
            ModbusCommand.Funct_CRC16(buffer, 6);
            buffer[6] = ModbusCommand.CRC16L;
            buffer[7] = ModbusCommand.CRC16H;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void modbus04(byte addr, int start_addr, int count) {
        int address = start_addr + ModbusCommand.MODBUS_ADDR_INPUT;
        try {
            buffer[0] = addr;
            buffer[1] = 4;
            buffer[2] = (byte) (address >> 8);
            buffer[3] = (byte) (address & 255);
            buffer[4] = (byte) (count >> 8);
            buffer[5] = (byte) (count & 255);
            ModbusCommand.Funct_CRC16(buffer, 6);
            buffer[6] = ModbusCommand.CRC16L;
            buffer[7] = ModbusCommand.CRC16H;
            Log.i("ROCK", "here:");
        } catch (Exception e) {
            Log.i("ROCK", "ERROR:" + e);
            e.printStackTrace();
        }
    }

    public static void modbus05(byte addr, int start_addr) {
        int value = 0;
        int address = start_addr + ModbusCommand.MODBUS_ADDR_COIL;
        try {
            if (command.valueCoil[start_addr] == 1) {
                value = 65280;
            }
            buffer[0] = addr;
            buffer[1] = 5;
            buffer[2] = (byte) (address >> 8);
            buffer[3] = (byte) (address & 255);
            buffer[4] = (byte) (value >> 8);
            buffer[5] = (byte) (value & 255);
            ModbusCommand.Funct_CRC16(buffer, 6);
            buffer[6] = ModbusCommand.CRC16L;
            buffer[7] = ModbusCommand.CRC16H;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void modbus06(byte addr, int start_addr) {
        int address = start_addr + ModbusCommand.MODBUS_ADDR_HOLD;
        try {
            buffer[0] = addr;
            buffer[1] = 6;
            buffer[2] = (byte) (address >> 8);
            buffer[3] = (byte) (address & 255);
            buffer[4] = (byte) (command.valueHold[start_addr] >> 8);
            buffer[5] = (byte) (command.valueHold[start_addr] & 255);
            ModbusCommand.Funct_CRC16(buffer, 6);
            buffer[6] = ModbusCommand.CRC16L;
            buffer[7] = ModbusCommand.CRC16H;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void modbus10(byte addr, int start_addr, int regCount) {
        int address = start_addr + ModbusCommand.MODBUS_ADDR_HOLD;
        int byteCount = regCount * 2;
        try {
            buffer[0] = addr;
            buffer[1] = 16;
            buffer[2] = (byte) (address >> 8);
            buffer[3] = (byte) (address & 255);
            buffer[4] = (byte) (regCount >> 8);
            buffer[5] = (byte) (regCount & 255);
            buffer[6] = (byte) (regCount * 2);
            int index = 0;
            for (int i = 0; i < byteCount; i += 2) {
                int data = command.valueHold[start_addr + index];
                buffer[i + 7] = (byte) (data >> 8);
                buffer[i + 8] = (byte) (data & 255);
                index++;
            }
            ModbusCommand.Funct_CRC16(buffer, byteCount + 7);
            buffer[byteCount + 7] = ModbusCommand.CRC16L;
            buffer[byteCount + 8] = ModbusCommand.CRC16H;
            StringBuffer str = new StringBuffer();
            for (int i2 = 0; i2 < 7; i2++) {
                str.append(String.format("%x ", Byte.valueOf(buffer[i2])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void modbus0F(byte addr, int start_addr, int regCount) {
        int byteCount;
        int address = start_addr + ModbusCommand.MODBUS_ADDR_COIL;
        try {
            if (regCount % 8 == 0) {
                byteCount = regCount / 8;
            } else {
                byteCount = (regCount / 8) + 1;
            }
            buffer[0] = addr;
            buffer[1] = 15;
            buffer[2] = (byte) (address >> 8);
            buffer[3] = (byte) (address & 255);
            buffer[4] = (byte) ((regCount >> 8) & 255);
            buffer[5] = (byte) (regCount & 255);
            buffer[6] = (byte) byteCount;
            int data = 0;
            int index = 0;
            int byteIndex = 0;
            for (int i = 0; i < regCount; i++) {
                if (command.valueCoil[start_addr + i] == 1) {
                    data |= 1 << index;
                }
                index++;
                if (index == 8) {
                    index = 0;
                    buffer[byteIndex + 7] = (byte) data;
                    byteIndex++;
                }
            }
            if (index > 0) {
                buffer[byteIndex + 7] = (byte) data;
            }
            ModbusCommand.Funct_CRC16(buffer, byteCount + 7);
            buffer[byteCount + 7] = ModbusCommand.CRC16L;
            buffer[byteCount + 8] = ModbusCommand.CRC16H;
            int i2 = byteCount + 9;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int saveData(byte[] buffer2, ModbusCommand modbusCommand) {
        if (command == null) {
            command = modbusCommand;
        }
        int length = buffer2.length;
        if (length > 0) {
            ModbusCommand.Funct_CRC16(buffer2, length - 2);
            if (buffer2[length - 2] != ModbusCommand.CRC16L || buffer2[length - 1] != ModbusCommand.CRC16H) {
                status = 1;
                Log.i("ROCK", "校验错误");
                return 3;
            } else if (buffer2[1] == ((byte) (funcCode + 128))) {
                status = 2;
                Log.i("ROCK", "接收错误");
                return 4;
            } else {
                status = 0;
                Log.i("ROCK", "接收成功");
                modbusDecode(raddr, rcount, buffer2);
                if (!command.isEmpty()) {
                    command.removeFirst();
                }
                Log.i("Rock", "数据长度：" + modbusCommand.cmdList.size());
                if (status == 0) {
                    if (!command.isEmpty()) {
                        return 2;
                    }
                    Log.i("ROCK", "返回:type=1");
                    return 1;
                }
            }
        }
        return -1;
    }

    public static void modbusDecode(int address, int rcount2, byte[] buffer2) {
        try {
            if (buffer2[1] == 2) {
                int index = 3;
                int bitIndex = 0;
                int tmpdata = buffer2[3] & 255;
                for (int i = 0; i < rcount2; i++) {
                    if (((1 << bitIndex) & tmpdata) > 0) {
                        command.valueDisc[address + i] = 1;
                    } else {
                        command.valueDisc[address + i] = 0;
                    }
                    bitIndex++;
                    if (bitIndex == 8) {
                        bitIndex = 0;
                        index++;
                        tmpdata = buffer2[index] & 255;
                    }
                }
            } else if (buffer2[1] == 1) {
                int index2 = 3;
                int bitIndex2 = 0;
                int tmpdata2 = buffer2[3] & 255;
                for (int i2 = 0; i2 < rcount2; i2++) {
                    if (((1 << bitIndex2) & tmpdata2) > 0) {
                        command.valueCoil[address + i2] = 1;
                    } else {
                        command.valueCoil[address + i2] = 0;
                    }
                    bitIndex2++;
                    if (bitIndex2 == 8) {
                        bitIndex2 = 0;
                        index2++;
                        tmpdata2 = buffer2[index2] & 255;
                    }
                }
            } else if (buffer2[1] == 4) {
                int index3 = 0;
                int count = rcount2 * 2;
                Log.i("ROCK", "这里的count:" + count);
                for (int i3 = 0; i3 < count; i3 += 2) {
                    Log.i("ROCK", "address + index:" + (address + index3) + "VALUE:" + (((buffer2[i3 + 3] & 255) << 8) + (buffer2[i3 + 4] & 255)));
                    Log.i("ROCK", ((int) buffer2[i3 + 3]) + "," + ((int) buffer2[i3 + 4]));
                    command.valueInput[address + index3] = ((buffer2[i3 + 3] & 255) << 8) + (buffer2[i3 + 4] & 255);
                    index3++;
                }
            } else if (buffer2[1] == 3) {
                int index4 = 0;
                int count2 = rcount2 * 2;
                for (int i4 = 0; i4 < count2; i4 += 2) {
                    Log.i("ROCK", "address + index:" + (address + index4) + "VALUE:" + (((buffer2[i4 + 3] & 255) << 8) + (buffer2[i4 + 4] & 255)));
                    Log.i("ROCK", ((int) buffer2[i4 + 3]) + "," + ((int) buffer2[i4 + 4]));
                    command.valueHold[address + index4] = ((buffer2[i4 + 3] & 255) << 8) + (buffer2[i4 + 4] & 255);
                    index4++;
                }
            }
        } catch (Exception E) {
            Log.i("Rock", "解析字节错误：" + E.getMessage());
        }
    }
}
