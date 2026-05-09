package bootstrap;

import application.usecase.admin.CreateDean;
import application.usecase.admin.CreateEmpResearcher;
import application.usecase.admin.CreateGradStudent;
import application.usecase.admin.CreateLibrarian;
import application.usecase.admin.CreateManager;
import application.usecase.admin.CreateStudent;
import application.usecase.admin.CreateTeacher;
import application.usecase.admin.CreateTechSupport;
import application.usecase.admin.DeleteUser;
import application.usecase.admin.GenerateAcademicReport;
import application.usecase.admin.GenerateTopResearcherNews;
import application.usecase.course.AddLesson;
import application.usecase.course.AssignTeacher;
import application.usecase.course.CreateCourse;
import application.usecase.course.DropCourse;
import application.usecase.course.EnrollInCourse;
import application.usecase.course.RecordMarks;
import application.usecase.course.SetCourseCapacity;
import application.usecase.course.SetCoursePrerequisite;
import application.usecase.course.ViewTranscript;
import application.usecase.library.AddBook;
import application.usecase.library.BorrowBook;
import application.usecase.library.RemoveBook;
import application.usecase.library.ReturnBook;
import application.usecase.messaging.AcceptOrder;
import application.usecase.messaging.CommentOnNews;
import application.usecase.messaging.CompleteOrder;
import application.usecase.messaging.CreateITOrder;
import application.usecase.messaging.ProcessRequest;
import application.usecase.messaging.PublishNews;
import application.usecase.messaging.SendMessage;
import application.usecase.messaging.SubmitRequest;
import application.usecase.organization.CreateOrganization;
import application.usecase.organization.JoinOrganization;
import application.usecase.research.CreateResearchProject;
import application.usecase.research.GenerateCitation;
import application.usecase.research.JoinResearchProject;
import application.usecase.research.PublishPaper;
import application.usecase.research.SetSupervisor;
import application.usecase.research.SubscribeToJournal;
import application.usecase.research.UnsubscribeFromJournal;
import application.usecase.user.BecomeResearcher;
import application.usecase.user.ComplainAboutStudent;
import application.usecase.user.RateTeacher;
import domain.repository.*;
import domain.rules.*;
import domain.service.*;
import domain.shared.IdSequence;
import infrastructure.auth.AuthenticationService;
import infrastructure.auth.PasswordHasher;
import infrastructure.auth.PlainPasswordHasher;
import infrastructure.logging.Logger;
import infrastructure.logging.RepositoryLogger;
import infrastructure.persistence.database.Database;
import infrastructure.persistence.database.JsonFileDatabase;
import infrastructure.persistence.orm.repository.*;
import presentation.cli.Console;
import presentation.cli.StdConsole;
import presentation.cli.auth.LoginScreen;
import presentation.cli.menu.*;

import java.nio.file.Path;
import java.util.List;

public final class AppContext {
    public final Console console = new StdConsole();
    public final PasswordHasher passwordHasher = new PlainPasswordHasher();

    public final Database database;

    public final UserRepository userRepository;
    public final CourseRepository courseRepository;
    public final BookRepository bookRepository;
    public final MessageRepository messageRepository;
    public final NewsRepository newsRepository;
    public final RequestRepository requestRepository;
    public final OrderRepository orderRepository;
    public final ResearchPaperRepository paperRepository;
    public final ResearchProjectRepository projectRepository;
    public final OrganizationRepository orgRepository;
    public final NotificationRepository notificationRepository;
    public final LogRepository logRepository;

    public final Logger logger;
    public final AuthenticationService auth;

    public final IdSequence courseIds = new IdSequence();
    public final IdSequence bookIds = new IdSequence();
    public final IdSequence messageIds = new IdSequence();
    public final IdSequence newsIds = new IdSequence();
    public final IdSequence requestIds = new IdSequence();
    public final IdSequence orderIds = new IdSequence();
    public final IdSequence paperIds = new IdSequence();
    public final IdSequence projectIds = new IdSequence();

    public final EnrollmentService enrollmentService;
    public final RoomScheduler roomScheduler;
    public final HIndexCalculator hIndexCalculator = new HIndexCalculator();
    public final GpaCalculator gpaCalculator;
    public final CitationFormatter citationFormatter = new CitationFormatter();
    public final PaperPublisher paperPublisher;
    public final SubscriptionService subscriptionService;

    public final BecomeResearcher becomeResearcher;
    public final EnrollInCourse enrollInCourse;
    public final DropCourse dropCourse;
    public final RecordMarks recordMarks;
    public final CreateCourse createCourse;
    public final AssignTeacher assignTeacher;
    public final AddLesson addLesson;
    public final SetCoursePrerequisite setCoursePrerequisite;
    public final SetCourseCapacity setCourseCapacity;
    public final BorrowBook borrowBook;
    public final ReturnBook returnBook;
    public final AddBook addBook;
    public final RemoveBook removeBook;
    public final SendMessage sendMessage;
    public final SubmitRequest submitRequest;
    public final ProcessRequest processRequest;
    public final CreateITOrder createOrder;
    public final AcceptOrder acceptOrder;
    public final CompleteOrder completeOrder;
    public final PublishNews publishNews;
    public final PublishPaper publishPaper;
    public final CreateResearchProject createProject;
    public final SubscribeToJournal subscribeToJournal;
    public final UnsubscribeFromJournal unsubscribeFromJournal;
    public final GenerateCitation generateCitation;
    public final GenerateAcademicReport generateAcademicReport;
    public final GenerateTopResearcherNews generateTopResearcherNews;
    public final CreateStudent createStudent;
    public final CreateGradStudent createGradStudent;
    public final CreateTeacher createTeacher;
    public final CreateDean createDean;
    public final CreateManager createManager;
    public final CreateLibrarian createLibrarian;
    public final CreateTechSupport createTechSupport;
    public final CreateEmpResearcher createEmpResearcher;
    public final DeleteUser deleteUser;
    public final RateTeacher rateTeacher;
    public final ComplainAboutStudent complainAboutStudent;
    public final CreateOrganization createOrganization;
    public final JoinOrganization joinOrganization;
    public final JoinResearchProject joinResearchProject;
    public final SetSupervisor setSupervisor;
    public final CommentOnNews commentOnNews;
    public final ViewTranscript viewTranscript;

    public final BecomeResearcherAction becomeResearcherAction;
    public final ResearcherMenuExtension researcherMenu;
    public final LoginScreen loginScreen;
    public final MenuFactory menuFactory;

    public AppContext(Database database) {
        this.database = database;

        this.userRepository = new OrmUserRepository(database);
        this.courseRepository = new OrmCourseRepository(database);
        this.bookRepository = new OrmBookRepository(database);
        this.messageRepository = new OrmMessageRepository(database);
        this.newsRepository = new OrmNewsRepository(database);
        this.requestRepository = new OrmRequestRepository(database);
        this.orderRepository = new OrmOrderRepository(database);
        this.paperRepository = new OrmResearchPaperRepository(database);
        this.projectRepository = new OrmResearchProjectRepository(database);
        this.orgRepository = new OrmOrganizationRepository(database);
        this.notificationRepository = new OrmNotificationRepository(database);
        this.logRepository = new OrmLogRepository(database);

        this.logger = new RepositoryLogger(logRepository);
        this.auth = new AuthenticationService(userRepository, passwordHasher);

        this.enrollmentService = new EnrollmentService(List.of(
                new MaxFailLimitRule(),
                new AlreadyEnrolledRule(),
                new CreditLimitRule(),
                new PrerequisiteRule(courseRepository),
                new CapacityRule(),
                new ScheduleConflictRule(courseRepository)
        ));
        this.roomScheduler = new RoomScheduler(courseRepository);
        this.gpaCalculator = new GpaCalculator(courseRepository);
        this.paperPublisher = new PaperPublisher(paperRepository, projectRepository, notificationRepository, newsRepository, userRepository);
        this.subscriptionService = new SubscriptionService(projectRepository);

        this.becomeResearcher = new BecomeResearcher(logger);
        this.enrollInCourse = new EnrollInCourse(enrollmentService, courseRepository, userRepository, logger);
        this.dropCourse = new DropCourse(courseRepository, userRepository, logger);
        this.recordMarks = new RecordMarks(courseRepository, userRepository, logger);
        this.createCourse = new CreateCourse(courseRepository, courseIds, logger);
        this.assignTeacher = new AssignTeacher(courseRepository, userRepository, logger);
        this.addLesson = new AddLesson(courseRepository, roomScheduler, logger);
        this.setCoursePrerequisite = new SetCoursePrerequisite(courseRepository, logger);
        this.setCourseCapacity = new SetCourseCapacity(courseRepository, logger);
        this.borrowBook = new BorrowBook(bookRepository, logger);
        this.returnBook = new ReturnBook(bookRepository, logger);
        this.addBook = new AddBook(bookRepository, bookIds, logger);
        this.removeBook = new RemoveBook(bookRepository, logger);
        this.sendMessage = new SendMessage(messageRepository, userRepository, messageIds, logger);
        this.submitRequest = new SubmitRequest(requestRepository, requestIds, logger);
        this.processRequest = new ProcessRequest(requestRepository, messageRepository, messageIds, logger);
        this.createOrder = new CreateITOrder(orderRepository, orderIds, logger);
        this.acceptOrder = new AcceptOrder(orderRepository, logger);
        this.completeOrder = new CompleteOrder(orderRepository, messageRepository, messageIds, logger);
        this.publishNews = new PublishNews(newsRepository, newsIds, logger);
        this.publishPaper = new PublishPaper(paperPublisher, paperIds, logger);
        this.createProject = new CreateResearchProject(projectRepository, projectIds, logger);
        this.subscribeToJournal = new SubscribeToJournal(subscriptionService, userRepository, logger);
        this.unsubscribeFromJournal = new UnsubscribeFromJournal(subscriptionService, userRepository, logger);
        this.generateCitation = new GenerateCitation(paperRepository, citationFormatter);
        this.generateAcademicReport = new GenerateAcademicReport(courseRepository, userRepository, gpaCalculator, logger);
        this.generateTopResearcherNews = new GenerateTopResearcherNews(userRepository, paperRepository, newsRepository, hIndexCalculator, newsIds, logger);
        this.createStudent = new CreateStudent(userRepository, passwordHasher, logger);
        this.createGradStudent = new CreateGradStudent(userRepository, passwordHasher, logger);
        this.createTeacher = new CreateTeacher(userRepository, passwordHasher, logger);
        this.createDean = new CreateDean(userRepository, passwordHasher, logger);
        this.createManager = new CreateManager(userRepository, passwordHasher, logger);
        this.createLibrarian = new CreateLibrarian(userRepository, passwordHasher, logger);
        this.createTechSupport = new CreateTechSupport(userRepository, passwordHasher, logger);
        this.createEmpResearcher = new CreateEmpResearcher(userRepository, passwordHasher, logger);
        this.deleteUser = new DeleteUser(userRepository, logger);
        this.rateTeacher = new RateTeacher(userRepository, logger);
        this.complainAboutStudent = new ComplainAboutStudent(sendMessage, userRepository, logger);
        this.createOrganization = new CreateOrganization(orgRepository, logger);
        this.joinOrganization = new JoinOrganization(orgRepository, logger);
        this.joinResearchProject = new JoinResearchProject(projectRepository, logger);
        this.setSupervisor = new SetSupervisor(userRepository, paperRepository, hIndexCalculator, logger);
        this.commentOnNews = new CommentOnNews(newsRepository, logger);
        this.viewTranscript = new ViewTranscript(courseRepository, gpaCalculator);

        this.becomeResearcherAction = new BecomeResearcherAction(console, becomeResearcher, userRepository);
        this.researcherMenu = new ResearcherMenuExtension(console, publishPaper, createProject, subscribeToJournal, unsubscribeFromJournal, generateCitation, paperRepository);
        this.loginScreen = new LoginScreen(console, auth);
        this.menuFactory = new DefaultMenuFactory(this);
    }

    public static AppContext withJsonStorage(Path dir) { return new AppContext(new JsonFileDatabase(dir)); }
}
