package frc.robot;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.constants.Trajectory;
import frc.robot.util.FieldPoseUtils;
import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;
import org.littletonrobotics.junction.networktables.LoggedDashboardNumber;

public class Auto {
  private final LoggedDashboardChooser<Command> autoChooser;
  private final LoggedDashboardNumber autoDelay;

  private Command currentCommand;

  public Auto(Robot robot) {
    var drive = robot.drive;
    var shooter = robot.shooter;

    autoChooser = new LoggedDashboardChooser<>("Auto Choices");
    autoDelay = new LoggedDashboardNumber("Auto Delay", 0);

    // No-op
    autoChooser.addDefaultOption("None", new InstantCommand(() -> {}));

    // Autos
    autoChooser.addOption("Close side to Amp", drive.runToPose(FieldPoseUtils::alignedWithAmpPose));

    autoChooser.addOption(
        "Far side to Amp",
        new SequentialCommandGroup(
            drive.followPath(Trajectory.FarSideToAmp),
            drive.runToPose(FieldPoseUtils::alignedWithAmpPose)));

    autoChooser.addOption(
        "Close side to Amp to Source",
        new SequentialCommandGroup(
            drive.runToPose(FieldPoseUtils::alignedWithAmpPose),
            drive.followPath(Trajectory.AmpToSource),
            drive.runToPose(FieldPoseUtils::alignedWithSourcePose)));

    autoChooser.addOption(
        "Far side to Amp to Source",
        new SequentialCommandGroup(
            drive.followPath(Trajectory.FarSideToAmp),
            drive.runToPose(FieldPoseUtils::alignedWithAmpPose),
            drive.followPath(Trajectory.AmpToSource),
            drive.runToPose(FieldPoseUtils::alignedWithSourcePose)));

    autoChooser.addOption(
        "Close side to Amp to Middle",
        new SequentialCommandGroup(
            drive.runToPose(FieldPoseUtils::alignedWithAmpPose),
            drive.followPath(Trajectory.AmpToMiddle)));

    autoChooser.addOption(
        "Far side to Amp to Middle",
        new SequentialCommandGroup(
            drive.followPath(Trajectory.FarSideToAmp),
            drive.runToPose(FieldPoseUtils::alignedWithAmpPose),
            drive.followPath(Trajectory.AmpToMiddle)));

    autoChooser.addOption(
        "Far side to Source",
        new SequentialCommandGroup(
            drive.followPath(Trajectory.FarSideToSource),
            drive.runToPose(FieldPoseUtils::alignedWithSourcePose)));

    // SysId Routines
    autoChooser.addOption(
        "Drive SysId (Quasistatic Forward)",
        drive.sysIdQuasistatic(SysIdRoutine.Direction.kForward));
    autoChooser.addOption(
        "Drive SysId (Quasistatic Reverse)",
        drive.sysIdQuasistatic(SysIdRoutine.Direction.kReverse));
    autoChooser.addOption(
        "Drive SysId (Dynamic Forward)", drive.sysIdDynamic(SysIdRoutine.Direction.kForward));
    autoChooser.addOption(
        "Drive SysId (Dynamic Reverse)", drive.sysIdDynamic(SysIdRoutine.Direction.kReverse));

    autoChooser.addOption(
        "Shooter SysId (Quasistatic Forward)",
        shooter.flywheelSysIdQuasistatic(SysIdRoutine.Direction.kForward));
    autoChooser.addOption(
        "Shooter SysId (Quasistatic Reverse)",
        shooter.flywheelSysIdQuasistatic(SysIdRoutine.Direction.kReverse));
    autoChooser.addOption(
        "Shooter SysId (Dynamic Forward)",
        shooter.flywheelSysIdDynamic(SysIdRoutine.Direction.kForward));
    autoChooser.addOption(
        "Shooter SysId (Dynamic Reverse)",
        shooter.flywheelSysIdDynamic(SysIdRoutine.Direction.kReverse));
  }

  public void schedule() {
    if (autoDelay.get() != 0) {
      currentCommand =
          new SequentialCommandGroup(new WaitCommand(autoDelay.get()), autoChooser.get());
    } else if (autoChooser.get() == null) {
      currentCommand = new InstantCommand(() -> {});
    } else {
      currentCommand = autoChooser.get();
    }
    currentCommand.schedule();
  }

  public void cancel() {
    if (currentCommand != null) currentCommand.cancel();
  }
}
