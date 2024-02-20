package frc.robot.commands;

import com.choreo.lib.Choreo;
import com.choreo.lib.ChoreoTrajectory;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.*;
import frc.robot.constants.Trajectory;
import frc.robot.constants.TunableConstants;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.util.Alliance;
import frc.robot.util.FieldPoseUtils;
import java.util.Arrays;
import org.littletonrobotics.junction.Logger;

public class Autos {

  private static Command followPath(Drive drive, Trajectory trajectoryFile) {
    ChoreoTrajectory trajectory = Choreo.getTrajectory(trajectoryFile.fileName);

    return new SequentialCommandGroup(
        new DriveToPose(drive, () -> FieldPoseUtils.flipPoseIfRed(trajectory.getInitialPose())),
        new InstantCommand(
            () -> {
              Logger.recordOutput(
                  "Auto/TargetPose", FieldPoseUtils.flipPoseIfRed(trajectory.getFinalPose()));
              Logger.recordOutput(
                  "Auto/Trajectory",
                  Arrays.stream(trajectory.getPoses())
                      .map(FieldPoseUtils::flipPoseIfRed)
                      .toArray(Pose2d[]::new));
            }),
        Choreo.choreoSwerveCommand(
            trajectory,
            drive::getPose,
            new PIDController(TunableConstants.KpX, 0, 0),
            new PIDController(TunableConstants.KpY, 0, 0),
            new PIDController(TunableConstants.KpTheta, 0, 0),
            drive::runVelocity,
            Alliance::isRed));
  }

  public static Command CloseSideToAmp(Drive drive, Shooter shooter) {

    return new SequentialCommandGroup(
        new DriveToPose(drive, FieldPoseUtils::alignedWithAmpPose), ShootInAmp(shooter));
  }

  public static Command FarSideToAmp(Drive drive, Shooter shooter) {
    return new SequentialCommandGroup(
        followPath(drive, Trajectory.FarSideToAmp),
        new DriveToPose(drive, FieldPoseUtils::alignedWithAmpPose),
        ShootInAmp(shooter));
  }

  public static Command CloseSideToAmpToSource(Drive drive, Shooter shooter) {
    return new SequentialCommandGroup(
        new DriveToPose(drive, FieldPoseUtils::alignedWithAmpPose),
        ShootInAmp(shooter),
        followPath(drive, Trajectory.AmpToSource),
        new DriveToPose(drive, FieldPoseUtils::alignedWithSourcePose));
  }

  public static Command FarSideToAmpToSource(Drive drive, Shooter shooter) {
    return new SequentialCommandGroup(
        followPath(drive, Trajectory.FarSideToAmp),
        new DriveToPose(drive, FieldPoseUtils::alignedWithAmpPose),
        ShootInAmp(shooter),
        followPath(drive, Trajectory.AmpToSource),
        new DriveToPose(drive, FieldPoseUtils::alignedWithSourcePose));
  }

  public static Command CloseSideToAmpToMiddle(Drive drive, Shooter shooter) {
    return new SequentialCommandGroup(
        new DriveToPose(drive, FieldPoseUtils::alignedWithAmpPose),
        ShootInAmp(shooter),
        followPath(drive, Trajectory.AmpToMiddle));
  }

  public static Command FarSideToAmpToMiddle(Drive drive, Shooter shooter) {
    return new SequentialCommandGroup(
        followPath(drive, Trajectory.FarSideToAmp),
        new DriveToPose(drive, FieldPoseUtils::alignedWithAmpPose),
        ShootInAmp(shooter),
        followPath(drive, Trajectory.AmpToMiddle));
  }

  public static Command FarSideToSource(Drive drive) {
    return new SequentialCommandGroup(
        followPath(drive, Trajectory.FarSideToSource),
        new DriveToPose(drive, FieldPoseUtils::alignedWithSourcePose));
  }

  public static Command ShootInAmp(Shooter shooter) {
    // TODO: No-op for now.
    return new RunCommand(() -> {});
  }
}
