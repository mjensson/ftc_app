package com.qualcomm.ftcrobotcontroller.opmodes;

/**
 * Created by mjensson on 11/8/2015.
 */
import com.qualcomm.hardware.HiTechnicNxtDcMotorController;
import com.qualcomm.hardware.HiTechnicNxtServoController;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.util.Range;

public class MainTeleop extends OpMode {
    //Manipulator Servo Position
    double armEndServoPosition;

    // Manipulator Servo Delta value
    double armEndServoDelta = 0.01;

    DcMotorController driveController;

    DcMotor driveMotorLeft;
    DcMotor driveMotorRight;

    DcMotorController armController;
    DcMotor armRackMotor;

    ServoController armServoController;
    Servo armEndServo;

    int numOpLoops = 1;

    /*
    * Code to run when the op mode is first enabled goes here
    * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#init()
    */
    @Override
    public void init() {
        driveMotorLeft = hardwareMap.dcMotor.get("driveLeft");
        driveMotorRight = hardwareMap.dcMotor.get("driveRight");
        driveController = hardwareMap.dcMotorController.get("driveMotorController");

        armRackMotor = hardwareMap.dcMotor.get("armMotor");
        armController = hardwareMap.dcMotorController.get("armMotorController");

        armEndServo = hardwareMap.servo.get("armServo");
        armEndServoPosition = 0.1;
        armServoController = hardwareMap.servoController.get("armServoController");

        driveController.setMotorChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
        armController.setMotorChannelMode(DcMotorController.RunMode.RESET_ENCODERS);
    }

    /*
     * Code that runs repeatedly when the op mode is first enabled goes here
     * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#init_loop()
     */
    @Override
    public void init_loop() {

        driveMotorRight.setDirection(DcMotor.Direction.REVERSE);
        //driveMotorLeft.setDirection(DcMotor.Direction.REVERSE);

        driveMotorLeft.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        driveMotorRight.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        armRackMotor.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

    }

    /*
     * Code that runs once when the op mode is first enabled goes here
     * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#start()
     */
    @Override
    public void start() {

    }

    /*
     * This method will be called repeatedly in a loop
     * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#loop()
     */
    @Override
    public void loop() {

        if(driveController.getMotorControllerDeviceMode() == DcMotorController.DeviceMode.WRITE_ONLY){
            float left = -gamepad1.left_stick_y;
            float right = -gamepad1.right_stick_y;

            left = Range.clip(left, -1, 1);
            right = Range.clip(right, -1, 1);

            left = (float)scaleInput(left);
            right = (float)scaleInput(right);

            driveMotorLeft.setPower(left);
            driveMotorRight.setPower(right);
        }

        if(armController.getMotorControllerDeviceMode() == DcMotorController.DeviceMode.WRITE_ONLY) {
            float arm = -gamepad2.left_stick_y;

            arm = Range.clip(arm, -1, 1);

            arm = (float) scaleInput(arm);

            armRackMotor.setPower(arm);
        }

        if(gamepad2.x) {
            armEndServoPosition -= armEndServoDelta;
        }

        if(gamepad2.b) {
            armEndServoPosition += armEndServoDelta;
        }

        armEndServoPosition = Range.clip(armEndServoPosition, 0, 1);

        armEndServo.setPosition(armEndServoPosition);

        if(numOpLoops % 15 == 0){
            driveController.setMotorControllerDeviceMode(DcMotorController.DeviceMode.READ_ONLY);
            armController.setMotorControllerDeviceMode(DcMotorController.DeviceMode.READ_ONLY);

            telemetry.addData("Text", "free flow text");
            telemetry.addData("left motor", driveMotorLeft.getPower());
            telemetry.addData("right motor", driveMotorRight.getPower());
            telemetry.addData("left motor rot", driveMotorLeft.getCurrentPosition());
            telemetry.addData("RunMode: ", driveMotorLeft.getChannelMode().toString());
            telemetry.addData("arm motor", armRackMotor.getPower());
            telemetry.addData("arm pos", armRackMotor.getCurrentPosition());
            telemetry.addData("servo pos", armEndServo.getPosition());

            driveController.setMotorControllerDeviceMode(DcMotorController.DeviceMode.WRITE_ONLY);
            armController.setMotorControllerDeviceMode(DcMotorController.DeviceMode.WRITE_ONLY);

            numOpLoops = 0;
        }



        numOpLoops++;
    }

    /*
     * Code that runs once when the op mode is disabled goes here
     * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#stop()
     */
    @Override
    public void stop() {

    }

    /*
    * This method scales the joystick input so for low joystick values, the
    * scaled value is less than linear.  This is to make it easier to drive
    * the robot more precisely at slower speeds.
    */
    double scaleInput(double dVal)  {
        double[] scaleArray = { 0.0, 0.05, 0.09, 0.10, 0.12, 0.15, 0.18, 0.24,
                0.30, 0.36, 0.43, 0.50, 0.60, 0.72, 0.85, 1.00, 1.00 };

        // get the corresponding index for the scaleInput array.
        int index = (int) (dVal * 16.0);

        // index should be positive.
        if (index < 0) {
            index = -index;
        }

        // index cannot exceed size of array minus 1.
        if (index > 16) {
            index = 16;
        }

        // get value from the array.
        double dScale = 0.0;
        if (dVal < 0) {
            dScale = -scaleArray[index];
        } else {
            dScale = scaleArray[index];
        }

        // return scaled value.
        return dScale;
    }
}
