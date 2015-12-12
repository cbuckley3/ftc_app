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
public class XenonSeven extends OpMode {

	//drive motor declarations
	DcMotor frontRight;
	DcMotor rearRight;
	DcMotor frontLeft;
	DcMotor rearLeft;

	//auxiliary motor declarations
	DcMotor auxMotor1, auxMotor2, auxMotor3;

	//servo declarations
	Servo servo1, servo2;

	//180 degree servo control variables
	final double servo2Delta = 0.005;
	double servo2Position = 0;

	/**
	 * Constructor
	 */
	public XenonSeven() {

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
		//auxiliary motor definitions
        auxMotor1 = hardwareMap.dcMotor.get("m5");
		auxMotor2 = hardwareMap.dcMotor.get("m6");
		auxMotor3 = hardwareMap.dcMotor.get("m7");

		//servo definitions
		servo1 = hardwareMap.servo.get("s1"); //servo 1 is a continuous rotation servo
		servo2 = hardwareMap.servo.get("s2"); //servo 2 is a standard 180 degree servo

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

		//aux motor controls
        if (gamepad2.x) auxMotor1.setPower(-1);
        else if (gamepad2.b) auxMotor1.setPower(1);
        else auxMotor1.setPower(0);

		if (gamepad2.y) auxMotor2.setPower(-1);
		else if (gamepad2.a) auxMotor2.setPower(1);
		else auxMotor2.setPower(0);

		if (gamepad2.right_bumper) auxMotor3.setPower(-1);
		else if (gamepad2.right_trigger > 0.35) auxMotor3.setPower(1);
		else auxMotor3.setPower(0);

		//servo controls
		if (gamepad2.dpad_down) servo1.setPosition(0);
		else if (gamepad2.dpad_up) servo1.setPosition(1);
		else servo1.setPosition(0.5);

		//for the 180 degree servo, we first read the gamepad and increment the servo position variable accordingly
		if (gamepad2.left_bumper) servo2Position -= servo2Delta;
		else if (gamepad2.left_trigger > 0.35) servo2Position += servo2Delta;

		//next, we clip the servo position values so that they stay inside the interval [0,1]
		servo2Position = Range.clip(servo2Position, 0, 1);

		//finally, we assign the updated position value to the servo
		servo2.setPosition(servo2Position);

		//drive variable assignments
		float majorThrottle = gamepad1.left_stick_y;
		float pivotThrottle = gamepad1.right_stick_x;

		// clip the right/left values so that the values never exceed +/- 1
		majorThrottle = Range.clip(majorThrottle, -1, 1);
		pivotThrottle = Range.clip(pivotThrottle, -1, 1);

		// scale the joystick value to make it easier to control the robot more precisely at slower speeds.
		majorThrottle = (float)scaleInput(majorThrottle);
		pivotThrottle =  (float)scaleInput(pivotThrottle);

		// write the values to the motors
		frontRight.setPower(majorThrottle + pivotThrottle);
		rearRight.setPower(majorThrottle + pivotThrottle);
		frontLeft.setPower(-1 * majorThrottle + pivotThrottle);
		rearLeft.setPower(-1 * majorThrottle + pivotThrottle);

		/*
		 * Send telemetry data back to driver station. Note that if we are using
		 * a legacy NXT-compatible motor controller, then the getPower() method
		 * will return a null value. The legacy NXT-compatible motor controllers
		 * are currently write only.
		 */
		telemetry.addData("Text", "The Xenon's code was written by Connor and Max.");
		telemetry.addData("Text", "No other data to report at this time.");

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
