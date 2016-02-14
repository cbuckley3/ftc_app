/* Copyright (c) 2014 Qualcomm Technologies Inc

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Qualcomm Technologies Inc nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

/**
 * TeleOp Mode
 * <p>
 * Enables control of the robot via the gamepad
 */
public class SarahTapeServos extends OpMode {

	//drive motor declarations
	DcMotor frontRight;
	DcMotor rearRight;
	DcMotor frontLeft;
	DcMotor rearLeft;

	//auxiliary motor declarations
	DcMotor winchExtension, winchPivot;

	//servo declarations
	Servo servo1, servo2, servo3, servo4;

	//motor throttle variables
	double rightThrottle, leftThrottle;

	//servo position and delta variables
	final double servoDelta = 0.005;
	double servo1Position, servo2Position, servo3Position, servo4Position;

	/**
	 * Constructor
	 */
	public SarahTapeServos() {

	}

	/*
	 * Code to run when the op mode is first enabled goes here
	 *
	 * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#start()
	 */
	@Override
	public void init() {


		/*
		 * Use the hardwareMap to get the dc motors and servos by name. Note
		 * that the names of the devices must match the names used when you
		 * configured your robot and created the configuration file.
		 */

		//drive motor definitions
		rearLeft = hardwareMap.dcMotor.get("m1");
		rearRight = hardwareMap.dcMotor.get("m2");
		frontLeft = hardwareMap.dcMotor.get("m3");
		frontRight = hardwareMap.dcMotor.get("m4");

		frontRight.setDirection(DcMotor.Direction.REVERSE);
		rearRight.setDirection(DcMotor.Direction.REVERSE);

		//auxiliary motor definitions
		winchExtension = hardwareMap.dcMotor.get("w1");
		winchPivot = hardwareMap.dcMotor.get("w2");

		//servo definitions
		servo1 = hardwareMap.servo.get("s1");
		servo2 = hardwareMap.servo.get("s2");
		servo3 = hardwareMap.servo.get("s3");
		servo4 = hardwareMap.servo.get("s4");

	}

	/*
	 * This method will be called repeatedly in a loop
	 *
	 * @see com.qualcomm.robotcore.eventloop.opmode.OpMode#run()
	 */
	@Override
	public void loop() {

		/*
		 * Gamepad 1
		 *
		 * Gamepad 1 controls the motors via the left stick, and it controls the
		 * wrist/claw via the a,b, x, y buttons
		 */

		//winch motor controls
        if (gamepad2.dpad_up) winchExtension.setPower(1);
		else if (gamepad2.dpad_down) winchExtension.setPower(-1);
		else winchExtension.setPower(0);

		if (gamepad2.dpad_left) winchPivot.setPower(-0.15);
        else if (gamepad2.dpad_right) winchPivot.setPower(0.15);
        else winchPivot.setPower(0);

		//servo controls
        if (gamepad2.b) servo1Position -= servoDelta;
        else if (gamepad2.x) servo1Position += servoDelta;

		if (gamepad2.a) servo2Position -= servoDelta;
		else if (gamepad2.y) servo2Position += servoDelta;

        if (gamepad2.left_bumper) servo3Position -= servoDelta;
        else if (gamepad2.left_trigger >= 0.25) servo3Position += servoDelta;

        if (gamepad2.right_bumper) servo4Position -= servoDelta;
        else if (gamepad2.right_trigger >= 0.25) servo4Position += servoDelta;

		//next, we clip the servo position values so that they stay inside the interval [0,1]
        servo1Position = Range.clip(servo1Position, 0, 1);
		servo2Position = Range.clip(servo2Position, 0, 1);
        servo3Position = Range.clip(servo3Position, 0, 1);
        servo4Position = Range.clip(servo4Position, 0, 1);

		//finally, we assign the updated position value to the servos
        servo1.setPosition(servo1Position);
		servo2.setPosition(servo2Position);
        servo3.setPosition(servo3Position);
        servo4.setPosition(servo4Position);

		//drive variable joystick assignments

		rightThrottle = gamepad1.right_stick_y;
		leftThrottle = gamepad1.left_stick_y;

		rightThrottle = Range.clip(rightThrottle, -1, 1);
		leftThrottle = Range.clip(leftThrottle, -1, 1);

		// scale the joystick value to make it easier to control the robot more precisely at slower speeds.
		rightThrottle = (float)scaleInput(rightThrottle);
		leftThrottle =  (float)scaleInput(leftThrottle);

		// write the values to the motors
		frontRight.setPower(rightThrottle);
		rearRight.setPower(rightThrottle);
		frontLeft.setPower(leftThrottle);
		rearLeft.setPower(leftThrottle);

		/*
		 * Send telemetry data back to driver station. Note that if we are using
		 * a legacy NXT-compatible motor controller, then the getPower() method
		 * will return a null value. The legacy NXT-compatible motor controllers
		 * are currently write only.
		 */
		telemetry.addData("Text", "The Xenon's code was written by Connor and Max. No other data to report at this time. Live long and prosper!");

	}

	/*
	 * Code to run when the op mode is first disabled goes here
	 *
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
