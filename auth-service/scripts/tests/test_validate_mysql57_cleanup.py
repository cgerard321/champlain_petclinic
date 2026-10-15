"""Test the actual terminal exit guard without executing Docker or fixture SQL."""
import ast
import pathlib
import unittest


SCRIPT = pathlib.Path(__file__).resolve().parents[1] / 'validate-mysql57-disposable-20261014.py'
TREE = ast.parse(SCRIPT.read_text())
FINAL_GUARD = TREE.body[-1]
assert isinstance(FINAL_GUARD, ast.If)
GUARD = compile(ast.Module(body=[FINAL_GUARD], type_ignores=[]), str(SCRIPT), 'exec')


class CleanupExitTest(unittest.TestCase):
    def exit_code(self, **changes):
        result = {
            'scenarios': [{'passed': True} for _ in range(7)],
            'cleanup_exit_code': 0,
            'cleanup_confirmed_absent': True,
        }
        result.update(changes)
        try:
            exec(GUARD, {'result': result})
        except SystemExit as exc:
            return exc.code
        return 0

    def test_success_requires_successful_cleanup_and_confirmed_absence(self):
        self.assertEqual(0, self.exit_code())

    def test_passing_scenarios_cannot_mask_cleanup_failure(self):
        self.assertEqual(1, self.exit_code(cleanup_exit_code=1))

    def test_passing_scenarios_cannot_mask_unconfirmed_absence(self):
        self.assertEqual(1, self.exit_code(cleanup_confirmed_absent=False))

    def test_both_cleanup_checks_failing_rejects_success(self):
        self.assertEqual(1, self.exit_code(cleanup_exit_code=1, cleanup_confirmed_absent=False))

    def test_missing_cleanup_status_rejects_success(self):
        result = {'scenarios': [{'passed': True} for _ in range(7)]}
        with self.assertRaises(SystemExit) as caught:
            exec(GUARD, {'result': result})
        self.assertEqual(1, caught.exception.code)

    def test_missing_individual_cleanup_fields_rejects_success(self):
        for missing in ('cleanup_exit_code', 'cleanup_confirmed_absent'):
            with self.subTest(missing=missing):
                result = {
                    'scenarios': [{'passed': True} for _ in range(7)],
                    'cleanup_exit_code': 0,
                    'cleanup_confirmed_absent': True,
                }
                del result[missing]
                with self.assertRaises(SystemExit) as caught:
                    exec(GUARD, {'result': result})
                self.assertEqual(1, caught.exception.code)

    def test_scenario_failure_still_rejects_success(self):
        self.assertEqual(1, self.exit_code(scenarios=[{'passed': False}] * 7))

    def test_incomplete_scenarios_still_reject_success(self):
        self.assertEqual(1, self.exit_code(scenarios=[{'passed': True}] * 6))

    def test_recorded_error_still_rejects_success(self):
        self.assertEqual(1, self.exit_code(error='synthetic error'))


if __name__ == '__main__':
    unittest.main()
