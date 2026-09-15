import { useState } from 'react';
import { 
  ShieldCheck, 
  Key, 
  Lock, 
  Folder, 
  Tag, 
  Search, 
  Zap, 
  FileCode, 
  Copy, 
  Check, 
  Download, 
  Database, 
  Terminal,
  ExternalLink,
  ChevronDown,
  ChevronRight,
  Eye,
  Server
} from 'lucide-react';

interface Endpoint {
  id: string;
  method: 'GET' | 'POST' | 'PUT' | 'DELETE';
  path: string;
  summary: string;
  description: string;
  requiresAuth: boolean;
  module: string;
  requestBody?: object;
  queryParams?: { name: string; type: string; required: boolean; description: string }[];
  pathParams?: { name: string; type: string; description: string }[];
  responses: { status: number; description: string; sample: object }[];
}

export default function App() {
  const [activeModule, setActiveModule] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [expandedEndpoints, setExpandedEndpoints] = useState<Record<string, boolean>>({
    'auth-register': true,
    'vault-unlock': true
  });
  const [bearerToken, setBearerToken] = useState<string>('');
  const [isAuthModalOpen, setIsAuthModalOpen] = useState<boolean>(false);
  const [copiedId, setCopiedId] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'endpoints' | 'schemas' | 'setup' | 'docker'>('endpoints');

  const modules = [
    { id: 'ALL', name: 'All Modules', icon: Server, count: 17 },
    { id: 'auth', name: '1. Authentication', icon: Key, count: 4 },
    { id: 'vault', name: '2. Vault Management', icon: Lock, count: 4 },
    { id: 'credentials', name: '3. Credential CRUD', icon: ShieldCheck, count: 5 },
    { id: 'folders', name: '4. Folders', icon: Folder, count: 4 },
    { id: 'tags', name: '5. Tags', icon: Tag, count: 4 },
    { id: 'search', name: '6. Vault Search', icon: Search, count: 1 },
    { id: 'generator', name: '7. Password Generator', icon: Zap, count: 1 },
    { id: 'audit', name: '8. Audit Logs', icon: FileCode, count: 1 },
  ];

  const endpoints: Endpoint[] = [
    // 1. Authentication
    {
      id: 'auth-register',
      module: 'auth',
      method: 'POST',
      path: '/auth/register',
      summary: 'Register User',
      description: 'Creates user account, generates unique Argon2id master key salt, and returns JWT token.',
      requiresAuth: false,
      requestBody: {
        username: "alice_security",
        email: "alice@example.com",
        password: "MasterAuthPassword123!"
      },
      responses: [
        {
          status: 201,
          description: "User registered successfully",
          sample: {
            success: true,
            message: "User registered successfully",
            data: {
              token: "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhbGljZV9zZWN1cml0eSIsImlhdCI6MTcz...",
              type: "Bearer",
              id: 1,
              username: "alice_security",
              email: "alice@example.com",
              isMasterPasswordSet: false,
              masterKeySalt: "dGVzdC1zYWx0LTE2LWJ5dGVzCg=="
            },
            timestamp: "2026-09-14T11:45:00"
          }
        },
        {
          status: 400,
          description: "Username or email already taken",
          sample: { success: false, status: 400, error: "Bad Request", message: "Username is already taken!" }
        }
      ]
    },
    {
      id: 'auth-login',
      module: 'auth',
      method: 'POST',
      path: '/auth/login',
      summary: 'Login User',
      description: 'Authenticates credentials using BCrypt and generates a signed JWT bearer token.',
      requiresAuth: false,
      requestBody: {
        usernameOrEmail: "alice_security",
        password: "MasterAuthPassword123!"
      },
      responses: [
        {
          status: 200,
          description: "Login successful",
          sample: {
            success: true,
            message: "Login successful",
            data: {
              token: "eyJhbGciOiJIUzI1NiIsIn...",
              type: "Bearer",
              id: 1,
              username: "alice_security",
              email: "alice@example.com",
              isMasterPasswordSet: true,
              masterKeySalt: "dGVzdC1zYWx0LTE2LWJ5dGVzCg=="
            }
          }
        },
        {
          status: 401,
          description: "Invalid credentials",
          sample: { success: false, status: 401, error: "Unauthorized", message: "Invalid username or password" }
        }
      ]
    },
    {
      id: 'auth-setup-master',
      module: 'auth',
      method: 'POST',
      path: '/auth/setup-master-password',
      summary: 'Master Password Setup (Zero-Knowledge)',
      description: 'Stores masterKeySalt, client-derived masterKeyVerifier hash, and initial encrypted vault key.',
      requiresAuth: true,
      requestBody: {
        masterKeySalt: "dGVzdC1zYWx0LTE2LWJ5dGVzCg==",
        masterKeyVerifier: "zkArgon2idDerivedVerifierHashToken992==",
        encryptedVaultKey: "AES256GCM_VAULT_KEY_BLOB=="
      },
      responses: [
        {
          status: 200,
          description: "Master password setup successful",
          sample: { success: true, message: "Master password successfully configured." }
        }
      ]
    },
    {
      id: 'auth-change-password',
      module: 'auth',
      method: 'POST',
      path: '/auth/change-password',
      summary: 'Change Password',
      description: 'Rotates user authentication password and updates master key verifier if specified.',
      requiresAuth: true,
      requestBody: {
        currentPassword: "MasterAuthPassword123!",
        newPassword: "BrandNewSecurePassword456!",
        newMasterKeySalt: "bmV3LXNhbHQtMTYtYnl0ZXMK",
        newMasterKeyVerifier: "newVerifierHashToken==",
        newEncryptedVaultKey: "NEW_ENCRYPTED_VAULT_KEY_BLOB=="
      },
      responses: [
        {
          status: 200,
          description: "Password changed successfully",
          sample: { success: true, message: "Password changed successfully." }
        }
      ]
    },

    // 2. Vault
    {
      id: 'vault-create',
      module: 'vault',
      method: 'POST',
      path: '/vault/create',
      summary: 'Create Vault',
      description: 'Initializes a new zero-knowledge encrypted vault with user symmetric key payload.',
      requiresAuth: true,
      requestBody: {
        encryptedVaultKey: "U2FsdGVkX1+vUpp3usGB1+hHG5fv2Re123456==",
        vaultSalt: "s0m3S4ltV4lu3B4s364=="
      },
      responses: [
        {
          status: 201,
          description: "Vault created successfully",
          sample: {
            success: true,
            message: "Vault created successfully",
            data: { id: 1, isLocked: false, vaultSalt: "s0m3S4ltV4lu3B4s364==", lastUnlockedAt: "2026-09-14T11:45:00" }
          }
        }
      ]
    },
    {
      id: 'vault-unlock',
      module: 'vault',
      method: 'POST',
      path: '/vault/unlock',
      summary: 'Unlock Vault',
      description: 'Validates zero-knowledge proof verifier token. Sets vault state to unlocked and logs VAULT_UNLOCKED.',
      requiresAuth: true,
      requestBody: {
        masterKeyVerifier: "zkArgon2idDerivedVerifierHashToken992=="
      },
      responses: [
        {
          status: 200,
          description: "Vault unlocked successfully",
          sample: {
            success: true,
            message: "Vault unlocked successfully",
            data: {
              id: 1,
              encryptedVaultKey: "U2FsdGVkX1+vUpp3usGB1+hHG5fv2Re123456==",
              vaultSalt: "s0m3S4ltV4lu3B4s364==",
              isLocked: false,
              lastUnlockedAt: "2026-09-14T11:45:30"
            }
          }
        },
        {
          status: 401,
          description: "Verifier token mismatch",
          sample: { success: false, status: 401, error: "Unauthorized", message: "Invalid master key verifier token. Access denied." }
        }
      ]
    },
    {
      id: 'vault-lock',
      module: 'vault',
      method: 'POST',
      path: '/vault/lock',
      summary: 'Lock Vault',
      description: 'Immediately locks the vault and revokes active session decryption state.',
      requiresAuth: true,
      responses: [
        {
          status: 200,
          description: "Vault locked successfully",
          sample: { success: true, message: "Vault locked successfully." }
        }
      ]
    },
    {
      id: 'vault-status',
      module: 'vault',
      method: 'GET',
      path: '/vault/status',
      summary: 'Vault Status',
      description: 'Returns vault initialization status, locked state, credential counts, and unlock timestamps.',
      requiresAuth: true,
      responses: [
        {
          status: 200,
          description: "Status retrieved",
          sample: {
            success: true,
            message: "Vault status retrieved",
            data: { initialized: true, locked: false, totalCredentials: 24, lastUnlockedAt: "2026-09-14T11:45:30" }
          }
        }
      ]
    },

    // 3. Credentials
    {
      id: 'credentials-create',
      module: 'credentials',
      method: 'POST',
      path: '/credentials',
      summary: 'Create Credential',
      description: 'Persists an encrypted credential item. Supports WEBSITE, DATABASE, API_KEYS, CLOUD, SERVER, SECURE_NOTES.',
      requiresAuth: true,
      requestBody: {
        name: "Production AWS Root Key",
        type: "CLOUD",
        url: "https://aws.amazon.com/console",
        username: "root_admin",
        encryptedPassword: "AQIDAHhGgR2...[AES-256-GCM IV+Ciphertext+Tag]...",
        notes: "Restricted to MFA hardware token",
        folderId: 1,
        tagNames: ["critical", "infrastructure"],
        favorite: true
      },
      responses: [
        {
          status: 201,
          description: "Credential created successfully",
          sample: {
            success: true,
            message: "Credential created successfully",
            data: {
              id: 101,
              name: "Production AWS Root Key",
              type: "CLOUD",
              username: "root_admin",
              encryptedPassword: "AQIDAHhGgR2...",
              favorite: true,
              folder: { id: 1, name: "Infrastructure" },
              tags: [{ id: 1, name: "critical", colorHex: "#EF4444" }],
              createdAt: "2026-09-14T11:46:00",
              passwordUpdated: "2026-09-14T11:46:00"
            }
          }
        }
      ]
    },
    {
      id: 'credentials-list',
      module: 'credentials',
      method: 'GET',
      path: '/credentials',
      summary: 'Get All Credentials',
      description: 'Retrieves all encrypted vault items for the authenticated user ordered by updated timestamp.',
      requiresAuth: true,
      responses: [
        {
          status: 200,
          description: "Credentials list",
          sample: {
            success: true,
            message: "Credentials retrieved successfully",
            data: [
              { id: 101, name: "Production AWS Root Key", type: "CLOUD", favorite: true },
              { id: 102, name: "GitHub Personal Token", type: "API_KEYS", favorite: false }
            ]
          }
        }
      ]
    },
    {
      id: 'credentials-get',
      module: 'credentials',
      method: 'GET',
      path: '/credentials/{id}',
      summary: 'Get Credential by ID',
      description: 'Retrieves single encrypted item, updates lastUsed timestamp, and logs PASSWORD_VIEWED audit event.',
      requiresAuth: true,
      pathParams: [{ name: 'id', type: 'Long', description: 'Credential ID' }],
      responses: [
        {
          status: 200,
          description: "Credential retrieved",
          sample: {
            success: true,
            message: "Credential retrieved successfully",
            data: { id: 101, name: "Production AWS Root Key", lastUsed: "2026-09-14T11:47:00" }
          }
        }
      ]
    },
    {
      id: 'credentials-update',
      module: 'credentials',
      method: 'PUT',
      path: '/credentials/{id}',
      summary: 'Update Credential',
      description: 'Updates encrypted payload, metadata, tags, and automatically stamps passwordUpdated on change.',
      requiresAuth: true,
      pathParams: [{ name: 'id', type: 'Long', description: 'Credential ID' }],
      requestBody: {
        name: "Production AWS Root Key (Rotated)",
        type: "CLOUD",
        encryptedPassword: "NEW_AES256GCM_ENCRYPTED_BLOB",
        tagNames: ["critical", "rotated"]
      },
      responses: [
        {
          status: 200,
          description: "Credential updated successfully",
          sample: { success: true, message: "Credential updated successfully", data: { id: 101 } }
        }
      ]
    },
    {
      id: 'credentials-delete',
      module: 'credentials',
      method: 'DELETE',
      path: '/credentials/{id}',
      summary: 'Delete Credential',
      description: 'Permanently deletes credential item and logs CREDENTIAL_DELETED event.',
      requiresAuth: true,
      pathParams: [{ name: 'id', type: 'Long', description: 'Credential ID' }],
      responses: [
        {
          status: 200,
          description: "Credential deleted successfully",
          sample: { success: true, message: "Credential deleted successfully." }
        }
      ]
    },

    // 4. Folders
    {
      id: 'folders-create',
      module: 'folders',
      method: 'POST',
      path: '/folders',
      summary: 'Create Folder',
      description: 'Creates a hierarchical folder to organize credentials.',
      requiresAuth: true,
      requestBody: { name: "Cloud & Databases", description: "AWS, Azure, and MySQL production keys" },
      responses: [
        {
          status: 201,
          description: "Folder created",
          sample: { success: true, message: "Folder created successfully", data: { id: 1, name: "Cloud & Databases" } }
        }
      ]
    },
    {
      id: 'folders-list',
      module: 'folders',
      method: 'GET',
      path: '/folders',
      summary: 'Get Folders',
      description: 'Returns all folders owned by authenticated user with item counts.',
      requiresAuth: true,
      responses: [
        {
          status: 200,
          description: "Folders retrieved",
          sample: {
            success: true,
            data: [{ id: 1, name: "Cloud & Databases", credentialCount: 4 }]
          }
        }
      ]
    },
    {
      id: 'folders-update',
      module: 'folders',
      method: 'PUT',
      path: '/folders/{id}',
      summary: 'Update Folder',
      description: 'Renames or updates folder description.',
      requiresAuth: true,
      pathParams: [{ name: 'id', type: 'Long', description: 'Folder ID' }],
      requestBody: { name: "Infrastructure & Cloud", description: "Updated description" },
      responses: [
        { status: 200, description: "Folder updated", sample: { success: true, message: "Folder updated successfully" } }
      ]
    },
    {
      id: 'folders-delete',
      module: 'folders',
      method: 'DELETE',
      path: '/folders/{id}',
      summary: 'Delete Folder',
      description: 'Deletes folder. Associated credentials have their folderId cleared without being deleted.',
      requiresAuth: true,
      pathParams: [{ name: 'id', type: 'Long', description: 'Folder ID' }],
      responses: [
        { status: 200, description: "Folder deleted", sample: { success: true, message: "Folder deleted successfully." } }
      ]
    },

    // 5. Tags
    {
      id: 'tags-create',
      module: 'tags',
      method: 'POST',
      path: '/tags',
      summary: 'Create Tag',
      description: 'Creates a custom color-coded tag label.',
      requiresAuth: true,
      requestBody: { name: "production", colorHex: "#EF4444" },
      responses: [
        { status: 201, description: "Tag created", sample: { success: true, data: { id: 1, name: "production", colorHex: "#EF4444" } } }
      ]
    },
    {
      id: 'tags-list',
      module: 'tags',
      method: 'GET',
      path: '/tags',
      summary: 'Get Tags',
      description: 'Returns all tags owned by authenticated user.',
      requiresAuth: true,
      responses: [
        { status: 200, description: "Tags list", sample: { success: true, data: [{ id: 1, name: "production", colorHex: "#EF4444" }] } }
      ]
    },
    {
      id: 'tags-update',
      module: 'tags',
      method: 'PUT',
      path: '/tags/{id}',
      summary: 'Update Tag',
      description: 'Updates tag name or color hex value.',
      requiresAuth: true,
      pathParams: [{ name: 'id', type: 'Long', description: 'Tag ID' }],
      requestBody: { name: "prod-critical", colorHex: "#B91C1C" },
      responses: [
        { status: 200, description: "Tag updated", sample: { success: true, message: "Tag updated successfully" } }
      ]
    },
    {
      id: 'tags-delete',
      module: 'tags',
      method: 'DELETE',
      path: '/tags/{id}',
      summary: 'Delete Tag',
      description: 'Deletes tag and disassociates from credentials.',
      requiresAuth: true,
      pathParams: [{ name: 'id', type: 'Long', description: 'Tag ID' }],
      responses: [
        { status: 200, description: "Tag deleted", sample: { success: true, message: "Tag deleted successfully." } }
      ]
    },

    // 6. Search
    {
      id: 'credentials-search',
      module: 'search',
      method: 'GET',
      path: '/credentials/search',
      summary: 'Search Credentials',
      description: 'Full multi-criteria search filtering by Name, URL, Username, Credential Type, Folder ID, and Tag.',
      requiresAuth: true,
      queryParams: [
        { name: 'name', type: 'String', required: false, description: 'Partial name match' },
        { name: 'url', type: 'String', required: false, description: 'Domain/URL match' },
        { name: 'username', type: 'String', required: false, description: 'Username match' },
        { name: 'type', type: 'CredentialType', required: false, description: 'WEBSITE, DATABASE, API_KEYS, CLOUD, SERVER, SECURE_NOTES' },
        { name: 'folderId', type: 'Long', required: false, description: 'Filter by folder' },
        { name: 'tag', type: 'String', required: false, description: 'Filter by tag name' }
      ],
      responses: [
        {
          status: 200,
          description: "Search results",
          sample: {
            success: true,
            message: "Search completed with 3 matches",
            data: [{ id: 101, name: "Production AWS Root Key", type: "CLOUD" }]
          }
        }
      ]
    },

    // 7. Password Generator
    {
      id: 'password-generate',
      module: 'generator',
      method: 'POST',
      path: '/password/generate',
      summary: 'Generate Password',
      description: 'Generates cryptographically random passwords with Shannon entropy scoring and ambiguous character filtering.',
      requiresAuth: false,
      requestBody: {
        length: 20,
        uppercase: true,
        lowercase: true,
        numbers: true,
        symbols: true,
        excludeAmbiguous: true
      },
      responses: [
        {
          status: 200,
          description: "Password generated",
          sample: {
            success: true,
            message: "Password generated successfully",
            data: {
              password: "k$9W#mP2x!B8vQ4@zL7n",
              length: 20,
              entropyBits: 131.08,
              strength: "Very Strong"
            }
          }
        }
      ]
    },

    // 8. Audit
    {
      id: 'audit-get',
      module: 'audit',
      method: 'GET',
      path: '/audit',
      summary: 'Get Audit Logs',
      description: 'Retrieves immutable security events (LOGIN, VAULT_UNLOCKED, PASSWORD_VIEWED, CREDENTIAL_CREATED, etc.) with IP and User-Agent. Zero plaintext passwords.',
      requiresAuth: true,
      queryParams: [
        { name: 'page', type: 'int', required: false, description: 'Page number (default: 0)' },
        { name: 'size', type: 'int', required: false, description: 'Page size (default: 20)' },
        { name: 'all', type: 'boolean', required: false, description: 'Retrieve unpaged list (default: false)' }
      ],
      responses: [
        {
          status: 200,
          description: "Audit logs retrieved",
          sample: {
            success: true,
            data: {
              content: [
                {
                  id: 1,
                  eventType: "VAULT_UNLOCKED",
                  description: "Vault successfully unlocked",
                  ipAddress: "192.168.1.105",
                  userAgent: "PostmanRuntime/7.39.0",
                  createdAt: "2026-09-14T11:45:30"
                },
                {
                  id: 2,
                  eventType: "PASSWORD_VIEWED",
                  description: "Viewed credential: Production AWS Root Key",
                  ipAddress: "192.168.1.105",
                  createdAt: "2026-09-14T11:47:00"
                }
              ],
              totalElements: 2,
              totalPages: 1
            }
          }
        }
      ]
    }
  ];

  const toggleEndpoint = (id: string) => {
    setExpandedEndpoints(prev => ({ ...prev, [id]: !prev[id] }));
  };

  const copyText = (text: string, id: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const filteredEndpoints = endpoints.filter(ep => {
    const matchesModule = activeModule === 'ALL' || ep.module === activeModule;
    const matchesSearch = searchQuery === '' || 
      ep.path.toLowerCase().includes(searchQuery.toLowerCase()) ||
      ep.summary.toLowerCase().includes(searchQuery.toLowerCase()) ||
      ep.method.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesModule && matchesSearch;
  });

  const getMethodBadge = (method: string) => {
    switch (method) {
      case 'GET':
        return 'bg-blue-900/60 text-blue-300 border-blue-700/80';
      case 'POST':
        return 'bg-emerald-900/60 text-emerald-300 border-emerald-700/80';
      case 'PUT':
        return 'bg-amber-900/60 text-amber-300 border-amber-700/80';
      case 'DELETE':
        return 'bg-rose-900/60 text-rose-300 border-rose-700/80';
      default:
        return 'bg-zinc-800 text-zinc-300 border-zinc-700';
    }
  };

  const downloadFile = (content: string, fileName: string, contentType: string) => {
    const a = document.createElement("a");
    const file = new Blob([content], { type: contentType });
    a.href = URL.createObjectURL(file);
    a.download = fileName;
    a.click();
    URL.revokeObjectURL(a.href);
  };

  return (
    <div id="api-explorer-root" className="min-h-screen bg-zinc-950 text-zinc-100 font-sans antialiased selection:bg-indigo-500 selection:text-white">
      {/* Top Header */}
      <header className="border-b border-zinc-800/80 bg-zinc-900/90 backdrop-blur sticky top-0 z-30">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-4 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-indigo-500 to-cyan-500 flex items-center justify-center shadow-lg shadow-indigo-500/20">
              <ShieldCheck className="w-6 h-6 text-white" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-xl font-bold tracking-tight text-white">Zero-Knowledge Password Manager</h1>
                <span className="text-xs font-mono font-medium px-2 py-0.5 rounded-full bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
                  v1.0.0
                </span>
                <span className="text-xs font-mono px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                  Spring Boot 3.3.4
                </span>
              </div>
              <p className="text-xs text-zinc-400 mt-0.5">
                Java 21 • MySQL 8 (Docker) • AES-256-GCM • Argon2id • Spring Security • OpenAPI 3.0
              </p>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="flex items-center flex-wrap gap-2.5">
            <button
              id="authorize-btn"
              onClick={() => setIsAuthModalOpen(true)}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-medium border flex items-center gap-2 transition-all ${
                bearerToken 
                  ? 'bg-emerald-500/15 border-emerald-500/40 text-emerald-300' 
                  : 'bg-zinc-800 hover:bg-zinc-700/80 border-zinc-700 text-zinc-200'
              }`}
            >
              <Key className="w-3.5 h-3.5" />
              {bearerToken ? 'Authorized (JWT Active)' : 'Authorize (JWT)'}
            </button>

            <a
              href="/postman_collection.json"
              download="postman_collection.json"
              className="px-3.5 py-1.5 rounded-lg text-xs font-medium bg-indigo-600 hover:bg-indigo-500 text-white flex items-center gap-1.5 shadow-sm transition-all"
            >
              <Download className="w-3.5 h-3.5" />
              Postman Collection
            </a>

            <button
              onClick={() => setActiveTab('docker')}
              className="px-3.5 py-1.5 rounded-lg text-xs font-medium bg-zinc-800 hover:bg-zinc-700 border border-zinc-700 text-zinc-200 flex items-center gap-1.5 transition-all"
            >
              <Database className="w-3.5 h-3.5 text-cyan-400" />
              Docker MySQL
            </button>
          </div>
        </div>

        {/* Navigation Tabs */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 flex gap-1 border-t border-zinc-800/60 pt-1">
          <button
            onClick={() => setActiveTab('endpoints')}
            className={`px-4 py-2 text-xs font-semibold rounded-t-lg transition-colors border-b-2 ${
              activeTab === 'endpoints'
                ? 'border-indigo-500 text-indigo-400 bg-zinc-800/40'
                : 'border-transparent text-zinc-400 hover:text-zinc-200'
            }`}
          >
            REST Endpoints ({endpoints.length})
          </button>
          <button
            onClick={() => setActiveTab('schemas')}
            className={`px-4 py-2 text-xs font-semibold rounded-t-lg transition-colors border-b-2 ${
              activeTab === 'schemas'
                ? 'border-indigo-500 text-indigo-400 bg-zinc-800/40'
                : 'border-transparent text-zinc-400 hover:text-zinc-200'
            }`}
          >
            DTO & Database Schema
          </button>
          <button
            onClick={() => setActiveTab('docker')}
            className={`px-4 py-2 text-xs font-semibold rounded-t-lg transition-colors border-b-2 ${
              activeTab === 'docker'
                ? 'border-indigo-500 text-indigo-400 bg-zinc-800/40'
                : 'border-transparent text-zinc-400 hover:text-zinc-200'
            }`}
          >
            Docker & Architecture
          </button>
          <button
            onClick={() => setActiveTab('setup')}
            className={`px-4 py-2 text-xs font-semibold rounded-t-lg transition-colors border-b-2 ${
              activeTab === 'setup'
                ? 'border-indigo-500 text-indigo-400 bg-zinc-800/40'
                : 'border-transparent text-zinc-400 hover:text-zinc-200'
            }`}
          >
            Setup Guide (Java 21)
          </button>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 py-6">
        {activeTab === 'endpoints' && (
          <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
            {/* Sidebar Modules Filter */}
            <div className="lg:col-span-1 space-y-2">
              <div className="sticky top-24 space-y-3">
                <div className="relative">
                  <Search className="w-4 h-4 text-zinc-400 absolute left-3 top-2.5" />
                  <input
                    type="text"
                    placeholder="Filter endpoints..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full pl-9 pr-3 py-2 text-xs bg-zinc-900 border border-zinc-800 rounded-lg text-zinc-100 placeholder-zinc-500 focus:outline-none focus:border-indigo-500"
                  />
                </div>

                <div className="p-3 bg-zinc-900/70 border border-zinc-800/80 rounded-xl space-y-1">
                  <div className="text-[10px] font-bold uppercase tracking-wider text-zinc-400 px-2 py-1">
                    Modules
                  </div>
                  {modules.map((m) => {
                    const Icon = m.icon;
                    const isActive = activeModule === m.id;
                    return (
                      <button
                        key={m.id}
                        onClick={() => setActiveModule(m.id)}
                        className={`w-full flex items-center justify-between px-2.5 py-1.5 rounded-lg text-xs font-medium transition-all text-left ${
                          isActive
                            ? 'bg-indigo-600/20 text-indigo-300 border border-indigo-500/30 font-semibold'
                            : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-800/50'
                        }`}
                      >
                        <div className="flex items-center gap-2 truncate">
                          <Icon className="w-3.5 h-3.5 shrink-0" />
                          <span className="truncate">{m.name}</span>
                        </div>
                        <span className="text-[10px] px-1.5 py-0.5 rounded bg-zinc-800 text-zinc-400 font-mono">
                          {m.count}
                        </span>
                      </button>
                    );
                  })}
                </div>

                {/* Base URL Box */}
                <div className="p-3 bg-zinc-900/50 border border-zinc-800/70 rounded-xl text-xs space-y-1.5">
                  <div className="text-[10px] font-bold text-zinc-400 uppercase tracking-wider">Default Host</div>
                  <div className="font-mono text-emerald-400 bg-zinc-950 px-2 py-1 rounded border border-zinc-800/80 text-[11px] select-all">
                    http://localhost:8080
                  </div>
                  <div className="text-[11px] text-zinc-500">
                    Swagger UI: <code className="text-zinc-400">/swagger-ui.html</code>
                  </div>
                </div>
              </div>
            </div>

            {/* Endpoints List */}
            <div className="lg:col-span-3 space-y-3">
              {filteredEndpoints.length === 0 ? (
                <div className="p-8 text-center bg-zinc-900/40 border border-zinc-800 rounded-xl text-zinc-400 text-sm">
                  No endpoints found matching "{searchQuery}"
                </div>
              ) : (
                filteredEndpoints.map((ep) => {
                  const isExpanded = !!expandedEndpoints[ep.id];
                  const curlCmd = `curl -X ${ep.method} "http://localhost:8080${ep.path}" \\
  -H "Content-Type: application/json"${ep.requiresAuth ? ` \\\n  -H "Authorization: Bearer ${bearerToken || '<YOUR_JWT_TOKEN>'}"` : ''}${ep.requestBody ? ` \\\n  -d '${JSON.stringify(ep.requestBody)}'` : ''}`;

                  return (
                    <div
                      key={ep.id}
                      className="border border-zinc-800/80 bg-zinc-900/60 hover:border-zinc-700/80 rounded-xl overflow-hidden transition-all shadow-sm"
                    >
                      {/* Endpoint Header Bar */}
                      <button
                        onClick={() => toggleEndpoint(ep.id)}
                        className="w-full flex items-center justify-between p-3.5 sm:p-4 text-left hover:bg-zinc-800/30 transition-colors"
                      >
                        <div className="flex items-center gap-3 flex-wrap">
                          <span className={`text-[11px] font-mono font-bold px-2.5 py-1 rounded-md border uppercase tracking-wide ${getMethodBadge(ep.method)}`}>
                            {ep.method}
                          </span>
                          <span className="font-mono text-sm font-semibold text-zinc-100">
                            {ep.path}
                          </span>
                          <span className="text-xs text-zinc-400 hidden md:inline">
                            {ep.summary}
                          </span>
                        </div>

                        <div className="flex items-center gap-2">
                          {ep.requiresAuth && (
                            <span className="flex items-center gap-1 text-[10px] font-mono font-medium px-2 py-0.5 rounded bg-amber-500/10 text-amber-400 border border-amber-500/20">
                              <Lock className="w-2.5 h-2.5" />
                              JWT
                            </span>
                          )}
                          {isExpanded ? (
                            <ChevronDown className="w-4 h-4 text-zinc-400" />
                          ) : (
                            <ChevronRight className="w-4 h-4 text-zinc-400" />
                          )}
                        </div>
                      </button>

                      {/* Expanded Endpoint Body */}
                      {isExpanded && (
                        <div className="border-t border-zinc-800/70 p-4 bg-zinc-950/40 space-y-4 text-xs">
                          <p className="text-zinc-300 leading-relaxed">{ep.description}</p>

                          {/* Query or Path Parameters */}
                          {(ep.pathParams || ep.queryParams) && (
                            <div className="space-y-2">
                              <div className="font-semibold text-zinc-200 text-[11px] uppercase tracking-wider">
                                Parameters
                              </div>
                              <div className="border border-zinc-800/80 rounded-lg overflow-hidden">
                                <table className="w-full text-left font-mono text-[11px]">
                                  <thead className="bg-zinc-900/80 text-zinc-400 border-b border-zinc-800">
                                    <tr>
                                      <th className="p-2 font-medium">Name</th>
                                      <th className="p-2 font-medium">In</th>
                                      <th className="p-2 font-medium">Type</th>
                                      <th className="p-2 font-medium">Description</th>
                                    </tr>
                                  </thead>
                                  <tbody className="divide-y divide-zinc-800/60 bg-zinc-950/50">
                                    {ep.pathParams?.map(p => (
                                      <tr key={p.name}>
                                        <td className="p-2 text-indigo-300 font-bold">{p.name}*</td>
                                        <td className="p-2 text-zinc-400">path</td>
                                        <td className="p-2 text-amber-400">{p.type}</td>
                                        <td className="p-2 text-zinc-300 font-sans">{p.description}</td>
                                      </tr>
                                    ))}
                                    {ep.queryParams?.map(q => (
                                      <tr key={q.name}>
                                        <td className="p-2 text-indigo-300">
                                          {q.name}{q.required ? '*' : ''}
                                        </td>
                                        <td className="p-2 text-zinc-400">query</td>
                                        <td className="p-2 text-amber-400">{q.type}</td>
                                        <td className="p-2 text-zinc-300 font-sans">{q.description}</td>
                                      </tr>
                                    ))}
                                  </tbody>
                                </table>
                              </div>
                            </div>
                          )}

                          {/* Request Body */}
                          {ep.requestBody && (
                            <div className="space-y-1.5">
                              <div className="flex items-center justify-between">
                                <span className="font-semibold text-zinc-200 text-[11px] uppercase tracking-wider">
                                  Request Body (JSON)
                                </span>
                                <button
                                  onClick={() => copyText(JSON.stringify(ep.requestBody, null, 2), `req-${ep.id}`)}
                                  className="text-zinc-400 hover:text-zinc-200 flex items-center gap-1 text-[11px]"
                                >
                                  {copiedId === `req-${ep.id}` ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
                                  Copy JSON
                                </button>
                              </div>
                              <pre className="p-3 bg-zinc-900 border border-zinc-800 rounded-lg font-mono text-[11px] text-zinc-200 overflow-x-auto">
                                {JSON.stringify(ep.requestBody, null, 2)}
                              </pre>
                            </div>
                          )}

                          {/* cURL Example */}
                          <div className="space-y-1.5">
                            <div className="flex items-center justify-between">
                              <span className="font-semibold text-zinc-300 text-[11px] uppercase tracking-wider flex items-center gap-1.5">
                                <Terminal className="w-3 h-3 text-cyan-400" />
                                Example cURL Request
                              </span>
                              <button
                                onClick={() => copyText(curlCmd, `curl-${ep.id}`)}
                                className="text-zinc-400 hover:text-zinc-200 flex items-center gap-1 text-[11px]"
                              >
                                {copiedId === `curl-${ep.id}` ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
                                Copy cURL
                              </button>
                            </div>
                            <pre className="p-3 bg-zinc-950 border border-zinc-800 rounded-lg font-mono text-[11px] text-cyan-300 overflow-x-auto whitespace-pre-wrap">
                              {curlCmd}
                            </pre>
                          </div>

                          {/* Responses */}
                          <div className="space-y-2">
                            <div className="font-semibold text-zinc-200 text-[11px] uppercase tracking-wider">
                              Responses
                            </div>
                            <div className="space-y-2">
                              {ep.responses.map((resp, i) => (
                                <div key={i} className="border border-zinc-800/80 rounded-lg overflow-hidden bg-zinc-900/60">
                                  <div className="px-3 py-1.5 bg-zinc-900 border-b border-zinc-800 flex items-center justify-between">
                                    <div className="flex items-center gap-2">
                                      <span className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded ${
                                        resp.status < 300 
                                          ? 'bg-emerald-900/60 text-emerald-300 border border-emerald-700/60' 
                                          : 'bg-rose-900/60 text-rose-300 border border-rose-700/60'
                                      }`}>
                                        {resp.status}
                                      </span>
                                      <span className="text-xs text-zinc-300">{resp.description}</span>
                                    </div>
                                    <button
                                      onClick={() => copyText(JSON.stringify(resp.sample, null, 2), `resp-${ep.id}-${i}`)}
                                      className="text-zinc-400 hover:text-zinc-200 flex items-center gap-1 text-[10px]"
                                    >
                                      {copiedId === `resp-${ep.id}-${i}` ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
                                      Copy
                                    </button>
                                  </div>
                                  <pre className="p-2.5 font-mono text-[11px] text-zinc-300 overflow-x-auto">
                                    {JSON.stringify(resp.sample, null, 2)}
                                  </pre>
                                </div>
                              ))}
                            </div>
                          </div>
                        </div>
                      )}
                    </div>
                  );
                })
              )}
            </div>
          </div>
        )}

        {/* Schemas Tab */}
        {activeTab === 'schemas' && (
          <div className="space-y-6">
            <div className="bg-zinc-900/80 border border-zinc-800 p-5 rounded-xl space-y-3">
              <h2 className="text-base font-bold text-white flex items-center gap-2">
                <Database className="w-5 h-5 text-cyan-400" />
                Relational MySQL Database Schema (JPA Entities)
              </h2>
              <p className="text-xs text-zinc-400">
                Created with InnoDB engine, utf8mb4 collation, foreign key constraints with ON DELETE CASCADE / SET NULL, and composite indexes for high-speed vault lookups.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Users Table */}
              <div className="p-4 bg-zinc-900/60 border border-zinc-800 rounded-xl space-y-2">
                <div className="flex items-center justify-between">
                  <span className="font-mono font-bold text-indigo-400 text-sm">Table: users</span>
                  <span className="text-[10px] bg-zinc-800 px-2 py-0.5 rounded text-zinc-400 font-mono">Entity: User</span>
                </div>
                <div className="font-mono text-xs text-zinc-300 space-y-1 bg-zinc-950 p-3 rounded-lg border border-zinc-800/80">
                  <div>id: BIGINT (PK, Auto-Increment)</div>
                  <div>username: VARCHAR(50) [UNIQUE]</div>
                  <div>email: VARCHAR(100) [UNIQUE]</div>
                  <div>password_hash: VARCHAR(255) [BCrypt]</div>
                  <div>master_key_salt: VARCHAR(128) [Argon2id Salt]</div>
                  <div>master_key_verifier: VARCHAR(255) [Zero-Knowledge Proof]</div>
                  <div>is_master_password_set: BOOLEAN</div>
                  <div>role: VARCHAR(20) [ROLE_USER]</div>
                  <div>created_at, updated_at: TIMESTAMP</div>
                </div>
              </div>

              {/* Vaults Table */}
              <div className="p-4 bg-zinc-900/60 border border-zinc-800 rounded-xl space-y-2">
                <div className="flex items-center justify-between">
                  <span className="font-mono font-bold text-indigo-400 text-sm">Table: vaults</span>
                  <span className="text-[10px] bg-zinc-800 px-2 py-0.5 rounded text-zinc-400 font-mono">Entity: Vault</span>
                </div>
                <div className="font-mono text-xs text-zinc-300 space-y-1 bg-zinc-950 p-3 rounded-lg border border-zinc-800/80">
                  <div>id: BIGINT (PK)</div>
                  <div>user_id: BIGINT [FK → users.id, UNIQUE, ON DELETE CASCADE]</div>
                  <div>encrypted_vault_key: TEXT [AES-256-GCM]</div>
                  <div>vault_salt: VARCHAR(128)</div>
                  <div>is_locked: BOOLEAN (Default: true)</div>
                  <div>last_unlocked_at: TIMESTAMP</div>
                  <div>created_at, updated_at: TIMESTAMP</div>
                </div>
              </div>

              {/* Credentials Table */}
              <div className="p-4 bg-zinc-900/60 border border-zinc-800 rounded-xl space-y-2">
                <div className="flex items-center justify-between">
                  <span className="font-mono font-bold text-indigo-400 text-sm">Table: credentials</span>
                  <span className="text-[10px] bg-zinc-800 px-2 py-0.5 rounded text-zinc-400 font-mono">Entity: Credential</span>
                </div>
                <div className="font-mono text-xs text-zinc-300 space-y-1 bg-zinc-950 p-3 rounded-lg border border-zinc-800/80">
                  <div>id: BIGINT (PK)</div>
                  <div>user_id: BIGINT [FK → users.id]</div>
                  <div>folder_id: BIGINT [FK → folders.id, ON DELETE SET NULL]</div>
                  <div>name: VARCHAR(150)</div>
                  <div>type: VARCHAR(30) [WEBSITE, DB, API_KEYS, CLOUD, SERVER, NOTES]</div>
                  <div>url: VARCHAR(500)</div>
                  <div>username: VARCHAR(150)</div>
                  <div>encrypted_password: TEXT [AES-256-GCM]</div>
                  <div>encrypted_notes: TEXT</div>
                  <div>favorite: BOOLEAN</div>
                  <div>last_used, password_updated: TIMESTAMP</div>
                </div>
              </div>

              {/* Audit Logs Table */}
              <div className="p-4 bg-zinc-900/60 border border-zinc-800 rounded-xl space-y-2">
                <div className="flex items-center justify-between">
                  <span className="font-mono font-bold text-indigo-400 text-sm">Table: audit_logs</span>
                  <span className="text-[10px] bg-zinc-800 px-2 py-0.5 rounded text-zinc-400 font-mono">Entity: AuditLog</span>
                </div>
                <div className="font-mono text-xs text-zinc-300 space-y-1 bg-zinc-950 p-3 rounded-lg border border-zinc-800/80">
                  <div>id: BIGINT (PK)</div>
                  <div>user_id: BIGINT [FK → users.id]</div>
                  <div>event_type: VARCHAR(50) [LOGIN, VAULT_UNLOCKED, PASSWORD_VIEWED, ...]</div>
                  <div>description: VARCHAR(255) [Never contains secrets]</div>
                  <div>ip_address: VARCHAR(45)</div>
                  <div>user_agent: VARCHAR(255)</div>
                  <div>created_at: TIMESTAMP</div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Docker & Architecture Tab */}
        {activeTab === 'docker' && (
          <div className="space-y-6">
            <div className="bg-zinc-900/80 border border-zinc-800 p-5 rounded-xl space-y-2">
              <h2 className="text-base font-bold text-white flex items-center gap-2">
                <Server className="w-5 h-5 text-indigo-400" />
                Zero-Knowledge Cryptographic Architecture
              </h2>
              <p className="text-xs text-zinc-400 leading-relaxed">
                In this zero-knowledge model, the server never receives or stores the master password or raw credential passwords.
                The client derives symmetric encryption keys with Argon2id and decrypts payloads locally.
              </p>
            </div>

            <div className="p-5 bg-zinc-900/50 border border-zinc-800 rounded-xl space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                  <FileCode className="w-4 h-4 text-cyan-400" />
                  docker-compose.yml (MySQL 8.0)
                </h3>
                <button
                  onClick={() => copyText(`version: '3.8'

services:
  mysql-db:
    image: mysql:8.0
    container_name: password_manager_mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: root_secure_pass
      MYSQL_DATABASE: password_manager_db
      MYSQL_USER: pm_user
      MYSQL_PASSWORD: pm_password_123
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./schema.sql:/docker-entrypoint-initdb.d/init.sql:ro
    command: --default-authentication-plugin=mysql_native_password --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

volumes:
  mysql_data:
    driver: local`, 'docker-compose')}
                  className="px-2.5 py-1 text-xs bg-zinc-800 hover:bg-zinc-700 text-zinc-300 rounded border border-zinc-700 flex items-center gap-1"
                >
                  {copiedId === 'docker-compose' ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                  Copy docker-compose.yml
                </button>
              </div>

              <pre className="p-4 bg-zinc-950 border border-zinc-800 rounded-lg font-mono text-xs text-zinc-300 overflow-x-auto">
{`version: '3.8'

services:
  mysql-db:
    image: mysql:8.0
    container_name: password_manager_mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: root_secure_pass
      MYSQL_DATABASE: password_manager_db
      MYSQL_USER: pm_user
      MYSQL_PASSWORD: pm_password_123
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./schema.sql:/docker-entrypoint-initdb.d/init.sql:ro
    command: --default-authentication-plugin=mysql_native_password --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

volumes:
  mysql_data:
    driver: local`}
              </pre>
            </div>
          </div>
        )}

        {/* Setup Guide Tab */}
        {activeTab === 'setup' && (
          <div className="space-y-6">
            <div className="bg-zinc-900/80 border border-zinc-800 p-5 rounded-xl space-y-2">
              <h2 className="text-base font-bold text-white flex items-center gap-2">
                <Terminal className="w-5 h-5 text-emerald-400" />
                How to Build & Run the Backend
              </h2>
              <p className="text-xs text-zinc-400">
                Follow these steps on your machine to start MySQL in Docker and launch the Spring Boot 3 / Java 21 backend application.
              </p>
            </div>

            <div className="space-y-4">
              <div className="p-4 bg-zinc-900/60 border border-zinc-800 rounded-xl space-y-2">
                <div className="text-xs font-bold text-indigo-400 uppercase tracking-wider">Step 1: Start MySQL in Docker</div>
                <pre className="p-3 bg-zinc-950 border border-zinc-800 rounded-lg font-mono text-xs text-emerald-400 select-all">
                  docker compose up -d
                </pre>
                <p className="text-xs text-zinc-400">Initializes the container and runs schema.sql automatically.</p>
              </div>

              <div className="p-4 bg-zinc-900/60 border border-zinc-800 rounded-xl space-y-2">
                <div className="text-xs font-bold text-indigo-400 uppercase tracking-wider">Step 2: Build with Maven (Java 21)</div>
                <pre className="p-3 bg-zinc-950 border border-zinc-800 rounded-lg font-mono text-xs text-emerald-400 select-all">
                  mvn clean package -DskipTests
                </pre>
              </div>

              <div className="p-4 bg-zinc-900/60 border border-zinc-800 rounded-xl space-y-2">
                <div className="text-xs font-bold text-indigo-400 uppercase tracking-wider">Step 3: Run Spring Boot</div>
                <pre className="p-3 bg-zinc-950 border border-zinc-800 rounded-lg font-mono text-xs text-emerald-400 select-all">
                  mvn spring-boot:run
                </pre>
                <p className="text-xs text-zinc-400">
                  The API starts on port 8080. Swagger UI is available at <code className="text-indigo-300">http://localhost:8080/swagger-ui.html</code>.
                </p>
              </div>

              <div className="p-4 bg-zinc-900/60 border border-zinc-800 rounded-xl space-y-2">
                <div className="text-xs font-bold text-indigo-400 uppercase tracking-wider">Step 4: Import into Postman</div>
                <p className="text-xs text-zinc-300 leading-relaxed">
                  Open Postman, click <strong>Import</strong>, and select the provided <code className="text-indigo-300">postman_collection.json</code> from the project root.
                  Running the register or login request will automatically save the JWT token into the collection variable <code className="text-indigo-300">{"{{token}}"}</code>.
                </p>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* JWT Authorization Modal */}
      {isAuthModalOpen && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-zinc-900 border border-zinc-800 rounded-xl max-w-md w-full p-5 space-y-4 shadow-2xl">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-bold text-white flex items-center gap-2">
                <Key className="w-4 h-4 text-emerald-400" />
                Set JWT Bearer Token
              </h3>
              <button
                onClick={() => setIsAuthModalOpen(false)}
                className="text-zinc-400 hover:text-zinc-200 text-xs"
              >
                ✕
              </button>
            </div>

            <p className="text-xs text-zinc-400">
              Paste the JWT token obtained from <code className="text-indigo-300">POST /auth/login</code> or <code className="text-indigo-300">POST /auth/register</code> to automatically include it in the generated cURL examples.
            </p>

            <div>
              <label className="text-[11px] font-semibold text-zinc-300 uppercase">Bearer Token</label>
              <textarea
                rows={4}
                value={bearerToken}
                onChange={(e) => setBearerToken(e.target.value.trim())}
                placeholder="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                className="w-full mt-1 p-2.5 bg-zinc-950 border border-zinc-800 rounded-lg text-xs font-mono text-zinc-200 focus:outline-none focus:border-indigo-500"
              />
            </div>

            <div className="flex items-center justify-end gap-2">
              <button
                onClick={() => { setBearerToken(''); setIsAuthModalOpen(false); }}
                className="px-3 py-1.5 rounded-lg text-xs font-medium text-zinc-400 hover:text-zinc-200"
              >
                Clear Token
              </button>
              <button
                onClick={() => setIsAuthModalOpen(false)}
                className="px-4 py-1.5 rounded-lg text-xs font-medium bg-indigo-600 hover:bg-indigo-500 text-white transition-colors"
              >
                Save & Apply
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
