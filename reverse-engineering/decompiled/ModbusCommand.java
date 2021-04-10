package com.kamoer.x1dosingpump.sockets;

import android.util.Log;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ModbusCommand implements Serializable {
    public static byte CRC16H = 0;
    public static byte CRC16L = 0;
    public static final int MODBUS_ADDR_COIL = 4097;
    public static final int MODBUS_ADDR_DISK = 8193;
    public static final int MODBUS_ADDR_HOLD = 12289;
    public static final int MODBUS_ADDR_INPUT = 16385;
    public static final int MODBUS_CLIENT_BUFFER_SIZE = 7;
    public static final int MODBUS_CLIENT_COIL_SIZE = 47;
    public static final int MODBUS_CLIENT_DISK_SIZE = 5;
    public static final int MODBUS_CLIENT_HOLD_SIZE = 52;
    public static final int MODBUS_CLIENT_INPUT_SIZE = 31;
    public static final int MODBUS_COMM_MAX_FAILED_TIMES = 4;
    public static final int MODBUS_DELAY_TIME_SETUP = 10;
    public static final int MODBUS_ERROR_ADDR = 3;
    public static final int MODBUS_ERROR_CRC = 1;
    public static final int MODBUS_ERROR_FUNC = 2;
    public static final int MODBUS_ERROR_OPERATE = 5;
    public static final int MODBUS_ERROR_TIME_OUT = 6;
    public static final int MODBUS_ERROR_VALUE = 4;
    public static final int MODBUS_SIZE_COIL = 20;
    public static final int MODBUS_SIZE_DISK = 20;
    public static final int MODBUS_SIZE_HOLD = 80;
    public static final int MODBUS_SIZE_INPUT = 20;
    public static final int MODBUS_SUCCESS = 0;
    public static final int MODBUS_TIME_OUT_SETUP = 3000;
    public static final int NET_CONNECT_FAILED = 11;
    public static final int NET_CONNECT_SUCCESS = 10;
    public static final int NET_CONNECT_TCPCLOSE = 12;
    private static ModbusCommand command = null;
    public boolean canWrite;
    public List<String> cmdList;
    private int commFaiedTimes;
    public boolean flag;
    public String ip;
    public int[] valueCoil;
    public int[] valueDisc;
    public int[] valueHold;
    public int[] valueInput;

    public ModbusCommand() {
        this.valueCoil = null;
        this.valueDisc = null;
        this.valueInput = null;
        this.valueHold = null;
        this.cmdList = null;
        this.commFaiedTimes = 0;
        this.canWrite = true;
        this.flag = false;
        this.valueCoil = new int[20];
        this.valueDisc = new int[20];
        this.valueHold = new int[80];
        this.valueInput = new int[20];
        this.cmdList = new ArrayList();
    }

    public static ModbusCommand getInstance() {
        if (command == null) {
            command = new ModbusCommand();
        }
        return command;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip2) {
        this.ip = ip2;
    }

    public void addCommand(String cmd) {
        Log.i("rock-cmd", cmd);
        this.canWrite = false;
        this.cmdList.add(cmd);
    }

    public void clearCommand() {
        this.cmdList.clear();
    }

    public boolean isEmpty() {
        return this.cmdList.isEmpty();
    }

    public void removeFirst() {
        this.cmdList.remove(0);
    }

    public void addCommFaiedTimes() {
        this.commFaiedTimes++;
    }

    public void clearCommFaiedTimes() {
        this.commFaiedTimes = 0;
    }

    public boolean isCommFailed() {
        return this.commFaiedTimes == 4;
    }

    public static int Funct_CRC16(byte[] bufData, int buflen) {
        int CRC = 65535;
        if (buflen != 0) {
            for (int i = 0; i < buflen; i++) {
                CRC ^= bufData[i] & 255;
                for (int j = 0; j < 8; j++) {
                    if ((CRC & 1) != 0) {
                        CRC = (CRC >> 1) ^ 40961;
                    } else {
                        CRC >>= 1;
                    }
                }
            }
            CRC16L = (byte) (CRC & 255);
            CRC16H = (byte) (CRC >> 8);
        }
        return 0;
    }
}
