/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.racetracker

import com.example.racetracker.ui.AppLogger
import com.example.racetracker.ui.RaceParticipant
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RaceParticipantTest {

    // A logger for tests that does nothing.
    class NoOpLogger : AppLogger {
        override fun e(tag: String, message: String) {
            // This method is empty, so it does nothing when called.
            // The test can run without crashing.
        }
    }

    private val raceParticipant = RaceParticipant(
        name = "Test",
        maxProgress = 100,
        progressDelayMillis = 500L,
        initialProgress = 0,
        progressIncrement = 1,
        logger = NoOpLogger()
    )

    /*
     *Since the test block needs to be placed in the runTest builder, use the expression
     *syntax to return the runTest() block as a test result.
     */

    ///Success path
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    ///function name format unitBeingTested_actionOrState_expectedResult
    fun raceParticipant_RaceStarted_ProgressUpdated()= runTest{
        val expectedProgress=1
        launch{ raceParticipant.run() }
        /*
        *You can directly call the raceParticipant.run() in the runtTest builder, but the
        *default test implementation ignores the call to delay(). As a result, the run()
        *finishes executing before you can analyze the progress.
        */

        /*
         * Use the advanceTimeBy() helper function to advance the time by the value of
         * raceParticipant.progressDelayMillis. The advanceTimeBy() function helps to reduce
         * the test execution time.
         */

        /*
         The run() function does get suspended because of delay(), and because runTest
         gives us manual control over the scheduler's clock(the runTest environment
         controls a virtual clock, that clock doesn't advance on its own.), we must
         manually move the time forward (advanceTimeBy) and then execute the pending
         tasks (runCurrent) to observe the result of the suspension ending.
         */
        advanceTimeBy(raceParticipant.progressDelayMillis)
        /*
         *Since advanceTimeBy() doesn't run the task scheduled at the given duration, you
         *need to call the runCurrent() function. This function executes any pending tasks
         *at the current time.
         */
        runCurrent()
        assertEquals(expectedProgress,raceParticipant.currentProgress)
    }

    ///Error Path
    @Test
    fun raceParticipant_RaceStartedAndForcedToStop_ProgressNotUpdated() = runTest {
        val expectedProgress = 0
        val job = launch {
            raceParticipant.run()
        }
        advanceTimeBy(250L)
        job.cancel()

        assertEquals(expectedProgress,raceParticipant.currentProgress)
    }


    ///Boundary Case
    @Test
    fun raceParticipant_RaceFinished_ProgressUpdated()=runTest {
        launch { raceParticipant.run() }
        advanceTimeBy(raceParticipant.maxProgress*raceParticipant.progressDelayMillis)
        runCurrent()
        assertEquals(100,raceParticipant.currentProgress)
    }
}