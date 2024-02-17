package frc.robot.commands;

import com.choreo.lib.Choreo;
import com.choreo.lib.ChoreoTrajectory;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.DeferredCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.Constants;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.shooter.ShooterSimple;
import frc.robot.util.Alliance;
import frc.robot.util.FieldPoseUtils;
import java.util.Set;

public class Autos {

  private static Command followPath(Drive drive, String pathName) {
    ChoreoTrajectory trajectory = Choreo.getTrajectory(pathName);

    return new SequentialCommandGroup(
        new DriveToPose(
            drive,
            Alliance.isRed()
                ? FieldPoseUtils.flipPose(trajectory.getInitialPose())
                : trajectory.getInitialPose()),
        Choreo.choreoSwerveCommand(
            trajectory,
            drive::getPose,
            new PIDController(Constants.DrivePIDConstants.KpX, 0, 0),
            new PIDController(Constants.DrivePIDConstants.KpY, 0, 0),
            new PIDController(Constants.DrivePIDConstants.KpTheta, 0, 0),
            drive::runVelocity,
            Alliance::isRed));
  }

  public static Command ScoreInAmpThenSource(Drive drive, ShooterSimple shooter) {
    return new DeferredCommand(
        () ->
            new SequentialCommandGroup(
                ScoreInAmp(drive, shooter),
                followPath(drive, "Amp to Source"),
                new DriveToPose(drive, FieldPoseUtils.alignedWithSourcePose())),
        Set.of(drive));
  }

  public static Command ScoreInAmp(Drive drive, ShooterSimple shooter) {
    return new DeferredCommand(
        () ->
            new SequentialCommandGroup(
                new DriveToPose(drive, FieldPoseUtils.alignedWithAmpPose()),
                new RunCommand(() -> shooter.setFlywheels(0.3, -0.3)).withTimeout(2)),
        Set.of(drive));
  }
}
