import unittest

from datetime import datetime, timedelta

from q1_alternativeimpl import MeanSlidingWindow


class MyTestCase(unittest.TestCase):
    def setUp(self) -> None:
        self.ms = MeanSlidingWindow()
        self.dt = datetime.fromisoformat('2020-01-01')

    def tearDown(self) -> None:
        self.ms = MeanSlidingWindow()
        self.dt = datetime.fromisoformat('2020-01-01')

    def test_mean(self):
        self.ms.add(self.dt, 1.0)
        self.ms.add(self.dt + timedelta(minutes=1), 2.0)
        self.assertEqual(self.ms.getMean(), 1.5)

    def test_resizing(self):
        self.ms.add(self.dt, 1.0)
        self.ms.add(self.dt + timedelta(minutes=1), 2.0)
        self.ms.add(self.dt + timedelta(minutes=2), 3.0)
        self.assertEqual(self.ms.getMean(), 2.0)

        self.ms.resize(self.dt)
        self.assertEqual(self.ms.getMean(), 2.0)

        self.ms.resize(self.dt + timedelta(minutes=1))
        self.assertEqual(self.ms.getMean(), 2.5)

    def test_active(self):
        self.ms.add(self.dt, 1.0)
        self.ms.add(self.dt + timedelta(minutes=1), 2.0)
        self.ms.add(self.dt + timedelta(minutes=2), 3.0)

        self.assertTrue(self.ms.active(self.dt + timedelta(minutes=2)))
        self.assertFalse(self.ms.active(self.dt + timedelta(minutes=3)))



if __name__ == '__main__':
    unittest.main()
