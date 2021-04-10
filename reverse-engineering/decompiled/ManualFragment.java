package com.kamoer.x1dosingpump.fragment;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.clj.fastble.data.ScanResult;
import com.clj.fastble.exception.BleException;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.kamoer.x1dosingpump.BuildConfig;
import com.kamoer.x1dosingpump.R;
import com.kamoer.x1dosingpump.application.MyApplication;
import com.kamoer.x1dosingpump.service.BluetoothService;
import com.kamoer.x1dosingpump.sockets.ModbusCommand;
import com.kamoer.x1dosingpump.utils.Constants;
import com.kamoer.x1dosingpump.utils.ReadWriteUilt;
import com.kamoer.x1dosingpump.utils.SharePreferenceUtil;
import com.kamoer.x1dosingpump.view.DialogWaiting;
import com.kamoer.x1dosingpump.view.RxDialogEditSureCancel;
import java.text.DecimalFormat;

public class ManualFragment extends BaseFragment implements BluetoothService.Callback {
    String MAC;
    @Bind({R.id.start_titration})
    Button btnStart;
    long counttime;
    DecimalFormat decimalFormat = new DecimalFormat("0.0");
    DialogWaiting dialogWaiting;
    @Bind({R.id.circle_progress})
    DonutProgress donutProgress;
    int flow;
    @Bind({R.id.frame_layout})
    FrameLayout frameLayout;
    LayoutInflater inflater;
    boolean isVisible = true;
    ServiceConnection mFhrSCon;
    View mView;
    @Bind({R.id.manual_titration_txt})
    TextView manualLiquidTxt;
    double manualVolume;
    ModbusCommand modbusCommand;
    MyCountDownTimer myCountDownTimer;
    String snKey;
    SharePreferenceUtil spUtil = null;
    int type;
    int volume;
    double volumeRemain;

    @Nullable
    public View onCreateView(LayoutInflater inflater2, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.mView = LayoutInflater.from(getActivity()).inflate(R.layout.view_manual_fragment, (ViewGroup) null);
        ButterKnife.bind(this, this.mView);
        this.dialogWaiting = new DialogWaiting(getActivity(), R.style.dialog_wating_style);
        this.modbusCommand = ModbusCommand.getInstance();
        this.spUtil = new SharePreferenceUtil(getActivity(), Constants.SP_NAME);
        this.snKey = this.spUtil.getString(Constants.SN, BuildConfig.FLAVOR);
        this.MAC = this.spUtil.getString(Constants.MAC, BuildConfig.FLAVOR);
        this.devicemac = this.MAC;
        this.mFhrSCon = new ServiceConnection() { // from class: com.kamoer.x1dosingpump.fragment.ManualFragment.1
            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i("ROCK", "serivce-connect:" + name);
                ManualFragment.this.mBluetoothService = ((BluetoothService.BluetoothBinder) service).getService();
                ManualFragment.this.mBluetoothService.setScanCallback(ManualFragment.this);
                Log.i("rock", "Connect-MAC:" + ManualFragment.this.MAC);
                ManualFragment.this.setService(ManualFragment.this.mBluetoothService);
                if (!MyApplication.bleManager.isConnected()) {
                    MyApplication.bleManager.closeBluetoothGatt();
                    ManualFragment.this.mBluetoothService.scanAndConnect5(ManualFragment.this.MAC);
                }
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName name) {
                Log.i("ROCK", "serivce-Disconnected:" + name);
                ManualFragment.this.mBluetoothService = null;
            }
        };
        if (this.mBluetoothService == null) {
            Log.i("ROCK", "mBluetoothService == null");
            bindService();
        } else {
            this.mBluetoothService.setScanCallback(this);
        }
        this.dialogWaiting.show();
        this.dialogWaiting.dissmissDialog(10000);
        return this.mView;
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisible = isVisibleToUser;
        if (!isVisibleToUser) {
            return;
        }
        if (this.mBluetoothService == null) {
            Log.i("ROCK", "mBluetoothService == null");
            bindService();
            return;
        }
        this.mBluetoothService.setScanCallback(this);
    }

    public void onResume() {
        super.onResume();
        if (this.mBluetoothService != null && this.isVisible) {
            this.mBluetoothService.setScanCallback(this);
        }
    }

    public boolean bindService() {
        Log.i("rock", "绑定服务");
        if (this.mFhrSCon == null) {
            return false;
        }
        Log.i("rock", "this.bindService");
        boolean isbind = getActivity().bindService(new Intent((Context) getActivity(), (Class<?>) BluetoothService.class), this.mFhrSCon, 1);
        Log.i("rock", "this.bindService:" + isbind);
        this.isBind = isbind;
        return isbind;
    }

    public void initData() {
        int state = this.modbusCommand.valueCoil[1];
        this.volumeRemain = ((double) this.modbusCommand.valueHold[51]) / 10.0d;
        this.manualVolume = ((double) this.modbusCommand.valueHold[50]) / 10.0d;
        this.flow = this.modbusCommand.valueHold[8];
        Log.i("Rock", "开关状态：" + state + "，剩余量：" + this.volumeRemain + ",手动跑的量：" + this.manualVolume + ",流量:" + this.flow);
        this.counttime = (long) (this.volumeRemain * ((double) this.flow));
        if (state == 1) {
            if (this.manualVolume != 0.0d) {
                this.donutProgress.setProgress((float) ((100.0d * this.volumeRemain) / this.manualVolume));
            } else {
                this.donutProgress.setProgress(0.0f);
            }
            this.manualLiquidTxt.setText(this.decimalFormat.format(this.volumeRemain) + BuildConfig.FLAVOR);
            this.btnStart.setText(getString(R.string.stop));
            this.myCountDownTimer = new MyCountDownTimer(this.counttime, 1000);
            this.myCountDownTimer.start();
            return;
        }
        this.donutProgress.setProgress(0.0f);
        this.manualLiquidTxt.setText(this.decimalFormat.format(this.manualVolume) + BuildConfig.FLAVOR);
    }

    @OnClick({R.id.start_titration, R.id.frame_layout})
    public void Click(View view) {
        if (verifyConnect()) {
            switch (view.getId()) {
                case R.id.frame_layout /* 2131624087 */:
                    if (this.btnStart.getText().toString().equals(getString(R.string.stop))) {
                        showToast(getActivity(), getString(R.string.can_not_set_manual_volume));
                        return;
                    }
                    final RxDialogEditSureCancel rxDialogEditSureCancel = new RxDialogEditSureCancel(getActivity(), 3);
                    rxDialogEditSureCancel.setTitle(getString(R.string.set_dosing_liquid_));
                    rxDialogEditSureCancel.getCancelView().setOnClickListener(new View.OnClickListener() { // from class: com.kamoer.x1dosingpump.fragment.ManualFragment.2
                        @Override // android.view.View.OnClickListener
                        public void onClick(View view2) {
                            rxDialogEditSureCancel.dismiss();
                        }
                    });
                    rxDialogEditSureCancel.getSureView().setOnClickListener(new View.OnClickListener() { // from class: com.kamoer.x1dosingpump.fragment.ManualFragment.3
                        @Override // android.view.View.OnClickListener
                        public void onClick(View view2) {
                            String content = rxDialogEditSureCancel.getEditText().getText().toString();
                            if (TextUtils.isEmpty(content)) {
                                BaseFragment.showToast(ManualFragment.this.getActivity(), ManualFragment.this.getString(R.string.value_is_num));
                            } else if (Double.parseDouble(content) == 0.0d) {
                                BaseFragment.showToast(ManualFragment.this.getActivity(), ManualFragment.this.getString(R.string.value_is_zero));
                            } else if (Double.parseDouble(content) > 6000.0d) {
                                BaseFragment.showToast(ManualFragment.this.getActivity(), ManualFragment.this.getString(R.string.value_is_too_large_then_6000));
                            } else {
                                ManualFragment.this.modbusCommand.clearCommand();
                                ManualFragment.this.manualVolume = (double) Float.parseFloat(content);
                                ManualFragment.this.manualLiquidTxt.setText(ManualFragment.this.decimalFormat.format(ManualFragment.this.manualVolume) + BuildConfig.FLAVOR);
                                ManualFragment.this.modbusCommand.valueHold[50] = (int) (Float.parseFloat(content) * 10.0f);
                                ManualFragment.this.modbusCommand.valueHold[49] = 0;
                                Log.i("需要设置的手动加液量：", ((int) (Float.parseFloat(content) * 10.0f)) + BuildConfig.FLAVOR);
                                ManualFragment.this.modbusCommand.valueCoil[2] = 1;
                                ManualFragment.this.modbusCommand.addCommand(String.format("%d %d %d", 1, 6, 50));
                                ManualFragment.this.modbusCommand.addCommand(String.format("%d %d %d", 1, 6, 49));
                                ManualFragment.this.modbusCommand.addCommand(String.format("%d %d %d", 1, 5, 2));
                                if (ManualFragment.this.mBluetoothService != null) {
                                    ManualFragment.this.write(ReadWriteUilt.getbytes(ManualFragment.this.modbusCommand));
                                }
                                ManualFragment.this.dialogWaiting.show();
                                ManualFragment.this.dialogWaiting.dissmissDialog(2000);
                                rxDialogEditSureCancel.dismiss();
                            }
                        }
                    });
                    rxDialogEditSureCancel.show();
                    return;
                case R.id.start_titration /* 2131624246 */:
                    this.modbusCommand.clearCommand();
                    if (this.btnStart.getText().equals(getString(R.string.start_dosing))) {
                        if (this.manualVolume == 0.0d) {
                            showToast(getActivity(), getString(R.string.please_set_titration));
                            return;
                        }
                        this.modbusCommand.clearCommand();
                        this.volumeRemain = this.manualVolume;
                        this.modbusCommand.valueCoil[1] = 1;
                        this.modbusCommand.valueHold[49] = 0;
                        this.modbusCommand.addCommand(String.format("%d %d %d", 1, 6, 49));
                        this.modbusCommand.addCommand(String.format("%d %d %d", 1, 5, 1));
                        if (this.mBluetoothService != null) {
                            write(ReadWriteUilt.getbytes(this.modbusCommand));
                        }
                        this.btnStart.setText(getString(R.string.stop));
                    } else if (this.btnStart.getText().equals(getString(R.string.stop))) {
                        this.modbusCommand.valueCoil[1] = 0;
                        this.modbusCommand.addCommand(String.format("%d %d %d", 1, 5, 1));
                        if (this.mBluetoothService != null) {
                            write(ReadWriteUilt.getbytes(this.modbusCommand));
                        }
                        this.btnStart.setText(getString(R.string.start_dosing));
                    }
                    this.dialogWaiting.show();
                    this.dialogWaiting.dissmissDialog(2000);
                    return;
                default:
                    return;
            }
        }
    }

    @Override // com.kamoer.x1dosingpump.service.BluetoothService.Callback
    public void onStartScan() {
    }

    @Override // com.kamoer.x1dosingpump.service.BluetoothService.Callback
    public void onScanning(ScanResult scanResult) {
    }

    @Override // com.kamoer.x1dosingpump.service.BluetoothService.Callback
    public void onScanComplete() {
    }

    @Override // com.kamoer.x1dosingpump.service.BluetoothService.Callback
    public void onConnecting() {
    }

    @Override // com.kamoer.x1dosingpump.service.BluetoothService.Callback
    public void onConnectFail(BleException bleException) {
    }

    @Override // com.kamoer.x1dosingpump.service.BluetoothService.Callback
    public void onConnectSuccess(BluetoothGatt gatt) {
    }

    @Override // com.kamoer.x1dosingpump.service.BluetoothService.Callback
    public void onDisConnected() {
    }

    @Override // com.kamoer.x1dosingpump.service.BluetoothService.Callback
    public void onServicesDiscovered(BluetoothGatt gatt) {
    }

    @Override // com.kamoer.x1dosingpump.service.BluetoothService.Callback
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] buffer = characteristic.getValue();
        if (this.isVisible && !this.isDestory) {
            this.type = ReadWriteUilt.saveData(characteristic.getValue(), this.modbusCommand);
            if (buffer.length > 4 && buffer[0] == 1 && buffer[1] == 3 && buffer[2] == 6) {
                if (this.dialogWaiting != null) {
                    this.dialogWaiting.dismiss();
                }
                initData();
            }
            if (buffer.length > 4 && buffer[0] == 1 && buffer[1] == 5 && buffer[2] == 16 && buffer[3] == 2) {
                if (this.dialogWaiting != null) {
                    this.dialogWaiting.dismiss();
                }
                Log.i("Rock", "buffer[4]:" + ((int) buffer[4]));
                this.flow = this.modbusCommand.valueHold[8];
                if (buffer[4] == 0) {
                    this.btnStart.setText(getString(R.string.start_dosing));
                    this.counttime = (long) (this.manualVolume * ((double) this.flow));
                    this.manualLiquidTxt.setText(this.decimalFormat.format(this.manualVolume) + BuildConfig.FLAVOR);
                    this.myCountDownTimer.onFinish();
                    this.myCountDownTimer.cancel();
                    this.myCountDownTimer = null;
                    this.donutProgress.setProgress(0.0f);
                } else if (buffer[4] == -1) {
                    if (this.myCountDownTimer != null) {
                        this.myCountDownTimer.onFinish();
                        this.myCountDownTimer = null;
                    }
                    this.counttime = (long) (this.manualVolume * ((double) this.flow));
                    this.volumeRemain = this.manualVolume;
                    Log.i("Rock", "时间间隔：" + this.counttime);
                    if (this.myCountDownTimer != null) {
                        this.myCountDownTimer.cancel();
                        this.myCountDownTimer.onFinish();
                        this.myCountDownTimer = null;
                    }
                    this.btnStart.setText(getString(R.string.stop));
                    this.myCountDownTimer = new MyCountDownTimer(this.counttime, 1000);
                    this.myCountDownTimer.start();
                }
            }
            if (this.type == 1) {
                if (this.dialogWaiting != null) {
                    this.dialogWaiting.dismiss();
                }
                if (buffer.length <= 4 || buffer[0] != 1 || buffer[1] != 5 || buffer[3] == 3) {
                }
            } else if (this.type == 2) {
                Log.i("Rock", "保存一组数据" + this.modbusCommand.cmdList.size());
                byte[] bytes = ReadWriteUilt.getbytes(this.modbusCommand);
                if (bytes != null && bytes.length > 0) {
                    write(ReadWriteUilt.getbytes(this.modbusCommand));
                }
            } else {
                if (this.type == 3) {
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override // android.os.CountDownTimer
        public void onTick(long l) {
            if (!ManualFragment.this.isDestory) {
                ManualFragment.this.counttime -= 1000;
                ManualFragment.this.volumeRemain -= 1000.0d / ((double) ManualFragment.this.flow);
                if (ManualFragment.this.volumeRemain < 0.0d) {
                    ManualFragment.this.volumeRemain = 0.0d;
                }
                try {
                    ManualFragment.this.donutProgress.setProgress((float) ((100.0d * (ManualFragment.this.manualVolume - ManualFragment.this.volumeRemain)) / ManualFragment.this.manualVolume));
                } catch (Exception e) {
                }
                ManualFragment.this.manualLiquidTxt.setText(ManualFragment.this.decimalFormat.format(ManualFragment.this.volumeRemain) + BuildConfig.FLAVOR);
            }
        }

        @Override // android.os.CountDownTimer
        public void onFinish() {
            if (!ManualFragment.this.isDestory) {
                ManualFragment.this.counttime = 0;
                ManualFragment.this.donutProgress.setProgress(0.0f);
                ManualFragment.this.volumeRemain = 0.0d;
                ManualFragment.this.manualLiquidTxt.setText(ManualFragment.this.decimalFormat.format(ManualFragment.this.manualVolume) + BuildConfig.FLAVOR);
                ManualFragment.this.btnStart.setText(ManualFragment.this.getString(R.string.start_dosing));
            }
        }
    }

    public void onDestroy() {
        this.isDestory = true;
        super.onDestroy();
    }
}
