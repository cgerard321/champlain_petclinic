using emailing_service.BackgroundTask;



using Moq;
using Microsoft.Extensions.Logging;
using NUnit.Framework;
using System;
using System.Reflection;
using System.Threading;
using System.Threading.Tasks;

namespace emailingservice_test.Background_Task{
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
            var expectedDelay = _service.GetType()
                .GetMethod("CalculateInitialDelay", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance)
                ?.Invoke(_service, new object[] { now });

            // Act
            await _service.StartAsync(stoppingToken);

            // Assert
            _mockLogger.Verify(logger => logger.Log(
                It.Is<LogLevel>(logLevel => logLevel == LogLevel.Information),
                It.IsAny<EventId>(),
                It.Is<It.IsAnyType>((state, t) => state.ToString().Contains("Recurring job started in")),
                It.IsAny<Exception>(),
                It.Is<Func<It.IsAnyType, Exception, string>>((state, ex) => true)),
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
            Assert.AreEqual(expectedDelay, result);
        }

        [Test]
        public void DoWork_LogsExecution()
        {
            // Act
            _service.GetType()
                .GetMethod("DoWork", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance)
                ?.Invoke(_service, new object[] { null });

            // Assert
            _mockLogger.Verify(logger => logger.Log(
                It.Is<LogLevel>(logLevel => logLevel == LogLevel.Information),
                It.IsAny<EventId>(),
                It.Is<It.IsAnyType>((state, t) => state.ToString().Contains("Recurring task executed")),
                It.IsAny<Exception>(),
                It.Is<Func<It.IsAnyType, Exception, string>>((state, ex) => true)),
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
            Assert.AreEqual(expectedDelay, result);
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
            Assert.AreEqual(expectedDelay, result);
        }







        [TearDown]
        public void Cleanup()
        {
            _service.Dispose();
            // Cleanup logic if necessary
        }
    }
    
}
