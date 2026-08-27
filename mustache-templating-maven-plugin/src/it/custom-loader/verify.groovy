def lineSeparator = System.lineSeparator()
def read = { path -> new File(basedir, path).getText('UTF-8') }

assert read('target/generated-text/test-5a.txt') ==
        "Test-5: Test mustache file: key1=value1; key2=value2; cheese=brie; fruit=apple; TEST FUNCTION${lineSeparator}"
assert read('target/generated-text/test-5b.txt') ==
        "Test-5: Test mustache file: key1=value1; key2=yale; cheese=cheddar; fruit=banana; TEST FUNCTION${lineSeparator}"
