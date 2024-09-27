using emailing_service.BackgroundTask;
using Microsoft.Extensions.Logging;
using Moq;

namespace emailing_service_test.Background_Task{
    [TestFixture]
    public class RecurringJobServiceTests
    {
        private Mock<ILogger<RecurringJobService>> _mockLogger;
        private RecurringJobService _service;

        [SetUp]
        public void Setup()
        {
            // Initialize the mock logger
            _mockLogger = new Mock<ILogger<RecurringJobService>>();

            // Create the service instance
            _service = new RecurringJobService(_mockLogger.Object);
        }

        [Test]
        public async Task ExecuteAsync_LogsInitialDelayAndStartsTimer()
        {
            // Arrange
            var stoppingToken = new CancellationTokenSource().Token;
            var now = DateTime.Now;
            //var expectedDelay =
            _service.GetType()
                .GetMethod("CalculateInitialDelay", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance)
                ?.Invoke(_service, new object[] { now });

            // Act
            await _service.StartAsync(stoppingToken);

            // Assert
            _mockLogger.Verify(logger => logger.Log(
                It.Is<LogLevel>(logLevel => logLevel == LogLevel.Information),
                It.IsAny<EventId>(),
                #pragma warning disable CS8602 // Dereference of a possibly null reference.
                It.Is<It.IsAnyType>((state, t) => state.ToString().Contains("Recurring job started in")),
                #pragma warning restore CS8602 // Dereference of a possibly null reference.
                It.IsAny<Exception>(),
                #pragma warning disable CS8620 // Argument cannot be used for parameter due to differences in the nullability of reference types.
                It.Is<Func<It.IsAnyType, Exception, string>>((state, ex) => true)),
                #pragma warning restore CS8620 // Argument cannot be used for parameter due to differences in the nullability of reference types.
                Times.Once);
        }

        [Test]
        public void CalculateInitialDelay_ReturnsCorrectDelay()
        {
            // Arrange
            var now = new DateTime(2024, 9, 22, 14, 15, 0); // Example time: 14:15
            var expectedDelay = TimeSpan.FromMinutes(15); // 15 minutes to the next 14:30 interval

            // Act
            var result = _service.GetType()
                .GetMethod("CalculateInitialDelay", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance)
                ?.Invoke(_service, new object[] { now });

            // Assert
            Assert.That(result, Is.EqualTo(expectedDelay));
        }

        [Test]
        public void DoWork_LogsExecution()
        {
            // Act
            _service.GetType()
                .GetMethod("DoWork", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance)
                ?.Invoke(_service, new object?[] { null });

            // Assert
            _mockLogger.Verify(logger => logger.Log(
                It.Is<LogLevel>(logLevel => logLevel == LogLevel.Information),
                It.IsAny<EventId>(),
                #pragma warning disable CS8602 // Dereference of a possibly null reference.
                It.Is<It.IsAnyType>((state, t) => state.ToString().Contains("Recurring task executed")),
                #pragma warning restore CS8602 // Dereference of a possibly null reference.
                It.IsAny<Exception>(),
                #pragma warning disable CS8620 // Argument cannot be used for parameter due to differences in the nullability of reference types.
                It.Is<Func<It.IsAnyType, Exception, string>>((state, ex) => true)),
                #pragma warning restore CS8620 // Argument cannot be used for parameter due to differences in the nullability of reference types.
                Times.Once);
        }
        [Test]
        public void CalculateInitialDelay_WhenAtExactInterval_SetsDelayToZero()
        {
            // Arrange
            var now = new DateTime(2024, 9, 22, 14, 30, 0); // Exactly at a round half-hour interval (14:30)
            var expectedDelay = TimeSpan.Zero; // The delay should be zero since we are already at the desired interval

            // Act
            var result = _service.GetType()
                .GetMethod("CalculateInitialDelay", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance)
                ?.Invoke(_service, new object[] { now });

            // Assert
            Assert.That(result, Is.EqualTo(expectedDelay));
        }
        [Test]
        public void CalculateInitialDelay_WhenAtExactInterval_ReturnsZeroDelay()
        {
            // Arrange
            var now = new DateTime(2024, 9, 22, 14, 00, 00); // Exactly at 14:00, a round hour interval
            var expectedDelay = TimeSpan.Zero; // Since it's exactly on the interval, the delay should be zero

            // Act
            var result = _service.GetType()
                .GetMethod("CalculateInitialDelay", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance)
                ?.Invoke(_service, new object[] { now });

            // Assert
            Assert.That(result, Is.EqualTo(expectedDelay));
        }







        [TearDown]
        public void Cleanup()
        {
            _service.Dispose();
            // Cleanup logic if necessary
        }
    }
    
}
