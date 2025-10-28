# NearYou ID - Design System

**Version:** 1.0  
**Last Updated:** 2025-10-27  
**For:** AI code generation + developer reference

> **Purpose:** This document provides complete UI specifications for generating consistent, high-quality Compose Multiplatform screens. Every pattern here is production-ready and tested.

---

## Color System

### Theme Definition

**Location:** `composeApp/src/commonMain/kotlin/id/nearyou/app/ui/theme/Color.kt`

```kotlin
// Light theme colors
val md_theme_light_primary = Color(0xFF006C4C)          // Main brand color
val md_theme_light_onPrimary = Color(0xFFFFFFFF)        // Text on primary
val md_theme_light_primaryContainer = Color(0xFF89F8C7) // Lighter primary variant
val md_theme_light_onPrimaryContainer = Color(0xFF002114) // Text on primaryContainer

val md_theme_light_secondary = Color(0xFF4D6357)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFCFE9D9)
val md_theme_light_onSecondaryContainer = Color(0xFF0A1F16)

val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onErrorContainer = Color(0xFF410002)

val md_theme_light_background = Color(0xFFFBFDF9)
val md_theme_light_onBackground = Color(0xFF191C1A)
val md_theme_light_surface = Color(0xFFFBFDF9)
val md_theme_light_onSurface = Color(0xFF191C1A)

// Dark theme colors
val md_theme_dark_primary = Color(0xFF56DCA9)
val md_theme_dark_onPrimary = Color(0xFF00382A)
val md_theme_dark_primaryContainer = Color(0xFF00513C)
val md_theme_dark_onPrimaryContainer = Color(0xFF89F8C7)

val md_theme_dark_secondary = Color(0xFFB3CCBD)
val md_theme_dark_onSecondary = Color(0xFF1F352A)
val md_theme_dark_secondaryContainer = Color(0xFF354B40)
val md_theme_dark_onSecondaryContainer = Color(0xFFCFE9D9)

val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)

val md_theme_dark_background = Color(0xFF191C1A)
val md_theme_dark_onBackground = Color(0xFFE1E3DF)
val md_theme_dark_surface = Color(0xFF191C1A)
val md_theme_dark_onSurface = Color(0xFFE1E3DF)
```

### Usage in Composables

```kotlin
// Surfaces and containers
Surface(
    color = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface
) {
    // Content
}

// Primary actions
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
) {
    Text("Primary Action")
}

// Error states
Text(
    text = "Error message",
    color = MaterialTheme.colorScheme.error
)
```

### Semantic Color Usage

| Use Case | Light Color | Dark Color | Purpose |
|----------|-------------|------------|---------|
| Main action buttons | `primary` | `primary` | CTAs, submit buttons |
| Secondary actions | `secondary` | `secondary` | Less prominent actions |
| Error messages | `error` | `error` | Validation errors, failures |
| Success (custom) | `Color(0xFF00C853)` | `Color(0xFF69F0AE)` | Success states |
| Distance badges | `primaryContainer` | `primaryContainer` | Location indicators |
| Online status | `Color(0xFF4CAF50)` | `Color(0xFF81C784)` | User presence |

---

## Typography

### Type Scale

**Location:** `composeApp/src/commonMain/kotlin/id/nearyou/app/ui/theme/Type.kt`

```kotlin
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

### Typography Usage Guidelines

```kotlin
// Screen titles
Text(
    text = "Create Account",
    style = MaterialTheme.typography.headlineLarge
)

// Section headers
Text(
    text = "Nearby Posts",
    style = MaterialTheme.typography.titleLarge
)

// Card titles
Text(
    text = "John Doe",
    style = MaterialTheme.typography.titleMedium,
    fontWeight = FontWeight.Bold
)

// Body content
Text(
    text = "Post content goes here...",
    style = MaterialTheme.typography.bodyMedium
)

// Metadata / timestamps
Text(
    text = "2 hours ago",
    style = MaterialTheme.typography.labelSmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)

// Button labels
Text(
    text = "Sign Up",
    style = MaterialTheme.typography.labelLarge
)
```

---

## Spacing System

**Location:** `composeApp/src/commonMain/kotlin/id/nearyou/app/ui/theme/Spacing.kt`

```kotlin
object Spacing {
    val none = 0.dp
    val xxxs = 2.dp
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val xxxl = 64.dp
}
```

### Spacing Usage

```kotlin
// Screen padding
Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(Spacing.md)  // 16.dp
) { /* ... */ }

// Component spacing
Column(
    verticalArrangement = Arrangement.spacedBy(Spacing.sm)  // 12.dp between items
) { /* ... */ }

// Button heights
Button(
    modifier = Modifier.height(Spacing.xl + Spacing.md)  // 48.dp
) { /* ... */ }
```

---

## Screen Patterns

### 1. Standard Screen Template

**Use for:** Most screens in the app

```kotlin
@Composable
fun ExampleScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExampleViewModel = koinInject()
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Screen Title") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    // Optional action buttons
                    IconButton(onClick = { /* action */ }) {
                        Icon(Icons.Default.MoreVert, "More options")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading && state.data == null -> {
                // Initial loading
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null && state.data == null -> {
                // Error state
                ErrorScreen(
                    error = state.error!!,
                    onRetry = viewModel::loadData,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            state.data != null -> {
                // Success state
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = Spacing.md)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Content goes here
                }
            }
        }
    }
}
```

### 2. Auth Screen Template

**Use for:** Login, signup, OTP verification

```kotlin
@Composable
fun AuthScreen(
    onNavigateNext: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinInject()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.lg),  // 24.dp
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo or icon
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(Spacing.lg))
        
        // Title
        Text(
            text = "Screen Title",
            style = MaterialTheme.typography.headlineLarge
        )
        
        // Subtitle
        Text(
            text = "Descriptive subtitle",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xs)
        )
        
        Spacer(modifier = Modifier.height(Spacing.xl))
        
        // Input fields
        // ...
        
        // Error message
        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.sm)
            )
        }
        
        // Primary action button
        Button(
            onClick = { /* action */ },
            enabled = state.isValid && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Primary Action")
            }
        }
        
        // Navigation links
        Spacer(modifier = Modifier.height(Spacing.md))
        
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = onNavigateBack) {
                Text("Log In")
            }
        }
    }
}
```

### 3. List Screen Template

**Use for:** Feed, conversations, search results

```kotlin
@Composable
fun ListScreen(
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ListViewModel = koinInject()
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    
    // Pagination logic
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && 
            lastVisibleItem.index >= state.items.size - 5 &&
            !state.isLoadingMore &&
            state.hasMore
        }
    }
    
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMore()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("List Title") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* create new */ }
            ) {
                Icon(Icons.Default.Add, "Create")
            }
        }
    ) { paddingValues ->
        when {
            state.isLoading && state.items.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null && state.items.isEmpty() -> {
                ErrorScreen(
                    error = state.error!!,
                    onRetry = viewModel::refresh,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            state.items.isEmpty() -> {
                EmptyScreen(
                    message = "No items found",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            else -> {
                SwipeRefresh(
                    state = rememberSwipeRefreshState(state.isRefreshing),
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            horizontal = Spacing.md,
                            vertical = Spacing.sm
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        items(
                            items = state.items,
                            key = { it.id }
                        ) { item ->
                            ItemCard(
                                item = item,
                                onClick = { onItemClick(item.id) }
                            )
                        }
                        
                        // Loading more indicator
                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.md),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

---

## Component Library

### Input Fields

#### Text Input

```kotlin
@Composable
fun TextInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        supportingText = {
            if (error != null) {
                Text(error)
            }
        },
        isError = error != null,
        enabled = enabled,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        modifier = modifier.fillMaxWidth()
    )
}

// Usage
TextInput(
    value = username,
    onValueChange = { username = it },
    label = "Username",
    placeholder = "johndoe",
    error = state.usernameError,
    modifier = Modifier.padding(bottom = Spacing.md)
)
```

#### Email Input

```kotlin
TextInput(
    value = email,
    onValueChange = { email = it },
    label = "Email",
    placeholder = "you@example.com",
    keyboardType = KeyboardType.Email,
    imeAction = ImeAction.Next
)
```

#### Phone Input

```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
) {
    // Country code
    OutlinedTextField(
        value = "+1",
        onValueChange = {},
        enabled = false,
        modifier = Modifier.width(70.dp),
        singleLine = true
    )
    
    // Phone number
    TextInput(
        value = phone,
        onValueChange = { phone = it },
        label = "Phone",
        placeholder = "234-567-8900",
        keyboardType = KeyboardType.Phone,
        modifier = Modifier.weight(1f)
    )
}
```

#### Password Input

```kotlin
var passwordVisible by remember { mutableStateOf(false) }

OutlinedTextField(
    value = password,
    onValueChange = { password = it },
    label = { Text("Password") },
    visualTransformation = if (passwordVisible) 
        VisualTransformation.None 
    else 
        PasswordVisualTransformation(),
    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Password,
        imeAction = ImeAction.Done
    ),
    trailingIcon = {
        IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
                imageVector = if (passwordVisible) 
                    Icons.Filled.Visibility 
                else 
                    Icons.Filled.VisibilityOff,
                contentDescription = if (passwordVisible) "Hide password" else "Show password"
            )
        }
    },
    singleLine = true,
    modifier = Modifier.fillMaxWidth()
)
```

### Buttons

#### Primary Button

```kotlin
@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    text: String,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(text)
        }
    }
}

// Usage
PrimaryButton(
    onClick = viewModel::submit,
    text = "Sign Up",
    isLoading = state.isLoading,
    enabled = state.isValid
)
```

#### Secondary Button

```kotlin
@Composable
fun SecondaryButton(
    onClick: () -> Unit,
    text: String,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        leadingIcon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
        }
        Text(text)
    }
}

// Usage
SecondaryButton(
    onClick = { /* google sign-in */ },
    text = "Continue with Google",
    leadingIcon = Icons.Default.AccountCircle  // Use Google icon in production
)
```

### Cards

#### Post Card

```kotlin
@Composable
fun PostCard(
    post: Post,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onComment),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md)
        ) {
            // User header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    onClick = onUserClick
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = post.authorUsername.first().uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(Spacing.sm))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.authorUsername,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(post.distanceMeters / 1000.0).format(1)} km away",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = post.createdAt.toRelativeTime(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.sm))
            
            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Media (if present)
            if (post.mediaUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                AsyncImage(
                    model = post.mediaUrls.first(),
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.sm))
            
            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    // Like button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
                    ) {
                        IconButton(onClick = onLike) {
                            Icon(
                                imageVector = if (post.isLikedByCurrentUser) 
                                    Icons.Filled.Favorite 
                                else 
                                    Icons.Outlined.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (post.isLikedByCurrentUser)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = post.likeCount.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Comment button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
                    ) {
                        IconButton(onClick = onComment) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Comment",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = post.commentCount.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Helper extension
fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
```

### Empty States

```kotlin
@Composable
fun EmptyScreen(
    message: String,
    icon: ImageVector = Icons.Outlined.Inbox,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(Spacing.md))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            Button(onClick = onAction) {
                Text(actionText)
            }
        }
    }
}
```

### Error States

```kotlin
@Composable
fun ErrorScreen(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(Spacing.md))
        
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(Spacing.xs))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Spacing.lg)
        )
        
        Spacer(modifier = Modifier.height(Spacing.lg))
        
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text("Try Again")
        }
    }
}
```

---

## Common Patterns

### Loading States

**Inline loading:**
```kotlin
if (state.isLoading) {
    LinearProgressIndicator(
        modifier = Modifier.fillMaxWidth()
    )
}
```

**Full-screen loading:**
```kotlin
Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
) {
    CircularProgressIndicator()
}
```

**Button loading:**
```kotlin
Button(onClick = { /* ... */ }) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        Text("Submit")
    }
}
```

### Snackbar Messages

```kotlin
val snackbarHostState = remember { SnackbarHostState() }
val scope = rememberCoroutineScope()

Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) }
) {
    // Content
}

// Show snackbar
LaunchedEffect(state.successMessage) {
    state.successMessage?.let { message ->
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }
}
```

### Dialogs

```kotlin
var showDialog by remember { mutableStateOf(false) }

if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        icon = { Icon(Icons.Default.Warning, contentDescription = null) },
        title = { Text("Delete Post?") },
        text = { Text("This action cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.deletePost()
                    showDialog = false
                }
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = { showDialog = false }) {
                Text("Cancel")
            }
        }
    )
}
```

---

## DO NOT Patterns (Anti-Patterns to Avoid)

### ❌ Magic Numbers

```kotlin
// Bad
.padding(16.dp)
.size(40.dp)
```

### ✅ Use Semantic Spacing

```kotlin
// Good
.padding(Spacing.md)
.size(40.dp)  // OK for specific sizes like avatars
```

### ❌ Inline Colors

```kotlin
// Bad
color = Color(0xFF123456)
```

### ✅ Use Theme Colors

```kotlin
// Good
color = MaterialTheme.colorScheme.primary
```

### ❌ Business Logic in Composables

```kotlin
// Bad
@Composable
fun LoginScreen() {
    Button(onClick = {
        scope.launch {
            val response = httpClient.post("/auth/login") { /* ... */ }
            // ...
        }
    })
}
```

### ✅ Use ViewModels

```kotlin
// Good
@Composable
fun LoginScreen(viewModel: AuthViewModel = koinInject()) {
    Button(onClick = viewModel::login)
}
```

### ❌ Ignoring Loading States

```kotlin
// Bad
Button(onClick = { viewModel.submit() }) {
    Text("Submit")
}
```

### ✅ Show Loading Feedback

```kotlin
// Good
Button(
    onClick = { viewModel.submit() },
    enabled = !state.isLoading
) {
    if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
    else Text("Submit")
}
```

---

## Platform-Specific Considerations

### Android

```kotlin
// Status bar color (in AndroidManifest.xml or via Window)
window.statusBarColor = MaterialTheme.colorScheme.surface.toArgb()

// Clear text traffic (development only)
<application android:usesCleartextTraffic="true">
```

### iOS

```kotlin
// Safe area handling
Column(
    modifier = Modifier
        .fillMaxSize()
        .safeDrawingPadding()
) {
    // Content automatically respects safe areas
}
```

---

## Accessibility

### Content Descriptions

```kotlin
// Icons
Icon(
    imageVector = Icons.Default.Search,
    contentDescription = "Search"
)

// Images
AsyncImage(
    model = url,
    contentDescription = "User profile picture"
)

// Decorative elements
Icon(
    imageVector = Icons.Default.Star,
    contentDescription = null  // Decorative only
)
```

### Semantic Properties

```kotlin
Text(
    text = "Important announcement",
    modifier = Modifier.semantics {
        contentDescription = "Important: $text"
        heading()
    }
)
```

---

## Testing UI Components

### Preview Examples

```kotlin
@Preview
@Composable
fun PreviewPostCard() {
    NearYouTheme {
        PostCard(
            post = Post(
                id = "1",
                authorId = "user1",
                authorUsername = "johndoe",
                content = "Great coffee shop!",
                latitude = 37.7749,
                longitude = -122.4194,
                distanceMeters = 250.5,
                likeCount = 42,
                commentCount = 7,
                isLikedByCurrentUser = false,
                createdAt = "2025-10-27T12:00:00Z",
                mediaUrls = emptyList()
            ),
            onLike = {},
            onComment = {},
            onUserClick = {}
        )
    }
}
```

---

**End of Design System**

> **Usage Note:** When generating new screens, copy relevant patterns from this document. Prioritize consistency over creativity. Every component should feel like it belongs in the same app.
