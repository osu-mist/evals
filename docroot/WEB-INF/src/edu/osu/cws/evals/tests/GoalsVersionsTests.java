package edu.osu.cws.evals.tests;

import edu.osu.cws.evals.models.GoalVersion;
import org.testng.annotations.Test;

import java.util.Date;

@Test
public class GoalsVersionsTests {

    public void shouldBeApprovedWhenApprovedDateIsNotNull() {
        GoalVersion goalVersion = new GoalVersion();
        goalVersion.setGoalsApprovedDate(new Date());
        assert goalVersion.areGoalsApproved();
    }

    public void shouldBeUnapprovedWhenRequestedIsApprovedButApprovedDateIsNull() {
        GoalVersion goalVersion = new GoalVersion();
        goalVersion.setRequestDecision(true);
        assert goalVersion.goalReactivationPendingOrApproved();
    }
}
