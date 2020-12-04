import unittest

from q1_alternativeimpl import MeanSlidingWindow


class MyTestCase(unittest.TestCase):
    def setUp(self) -> None:
        self.ms = MeanSlidingWindow()

    def test_something(self):
        self.assertEqual(True, False)


if __name__ == '__main__':
    unittest.main()
