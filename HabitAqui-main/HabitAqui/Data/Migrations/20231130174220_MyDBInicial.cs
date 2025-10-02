using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace HabitAqui.Data.Migrations
{
    public partial class MyDBInicial : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "EmpresaId",
                table: "AspNetUsers",
                type: "int",
                nullable: true);

            migrationBuilder.AddColumn<int>(
                name: "NIF",
                table: "AspNetUsers",
                type: "int",
                nullable: false,
                defaultValue: 0);

            migrationBuilder.AddColumn<string>(
                name: "NomeCompleto",
                table: "AspNetUsers",
                type: "nvarchar(max)",
                nullable: false,
                defaultValue: "");

            migrationBuilder.CreateTable(
                name: "Empresa",
                columns: table => new
                {
                    Id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    Nome = table.Column<string>(type: "nvarchar(max)", nullable: false),
                    Avaliacao = table.Column<double>(type: "float", nullable: false),
                    Ativo = table.Column<bool>(type: "bit", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Empresa", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "Habitacoes",
                columns: table => new
                {
                    Id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    Nome = table.Column<string>(type: "nvarchar(250)", maxLength: 250, nullable: false),
                    Descricao = table.Column<string>(type: "nvarchar(max)", maxLength: 5000, nullable: false),
                    DataInicio = table.Column<DateTime>(type: "datetime2", nullable: false),
                    DataFim = table.Column<DateTime>(type: "datetime2", nullable: false),
                    Localidade = table.Column<string>(type: "nvarchar(max)", maxLength: 5000, nullable: false),
                    Preco = table.Column<double>(type: "float", nullable: false),
                    Categoria = table.Column<string>(type: "nvarchar(max)", nullable: false),
                    PeriodoMinimoArrendamento = table.Column<int>(type: "int", nullable: false),
                    Estado = table.Column<string>(type: "nvarchar(max)", nullable: false),
                    EmpresaId = table.Column<int>(type: "int", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Habitacoes", x => x.Id);
                    table.ForeignKey(
                        name: "FK_Habitacoes_Empresa_EmpresaId",
                        column: x => x.EmpresaId,
                        principalTable: "Empresa",
                        principalColumn: "Id");
                });

            migrationBuilder.CreateTable(
                name: "Arrendamentos",
                columns: table => new
                {
                    Id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    Custo = table.Column<double>(type: "float", nullable: false),
                    UtilizadoresId = table.Column<string>(type: "nvarchar(450)", nullable: true),
                    EmpresaId = table.Column<int>(type: "int", nullable: true),
                    HabitacoesId = table.Column<int>(type: "int", nullable: true),
                    Estado = table.Column<string>(type: "nvarchar(max)", nullable: false),
                    EstadoInical = table.Column<string>(type: "nvarchar(100)", maxLength: 100, nullable: true),
                    EstadoFinal = table.Column<string>(type: "nvarchar(100)", maxLength: 100, nullable: true),
                    DataInicio = table.Column<DateTime>(type: "datetime2", nullable: false),
                    DataFim = table.Column<DateTime>(type: "datetime2", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Arrendamentos", x => x.Id);
                    table.ForeignKey(
                        name: "FK_Arrendamentos_AspNetUsers_UtilizadoresId",
                        column: x => x.UtilizadoresId,
                        principalTable: "AspNetUsers",
                        principalColumn: "Id");
                    table.ForeignKey(
                        name: "FK_Arrendamentos_Empresa_EmpresaId",
                        column: x => x.EmpresaId,
                        principalTable: "Empresa",
                        principalColumn: "Id");
                    table.ForeignKey(
                        name: "FK_Arrendamentos_Habitacoes_HabitacoesId",
                        column: x => x.HabitacoesId,
                        principalTable: "Habitacoes",
                        principalColumn: "Id");
                });

            migrationBuilder.CreateTable(
                name: "HabitacoesRating",
                columns: table => new
                {
                    ID = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    Comentario = table.Column<string>(type: "nvarchar(max)", maxLength: 5000, nullable: false),
                    HabitacoesId = table.Column<int>(type: "int", nullable: true),
                    UtilizadoresId = table.Column<string>(type: "nvarchar(450)", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_HabitacoesRating", x => x.ID);
                    table.ForeignKey(
                        name: "FK_HabitacoesRating_AspNetUsers_UtilizadoresId",
                        column: x => x.UtilizadoresId,
                        principalTable: "AspNetUsers",
                        principalColumn: "Id");
                    table.ForeignKey(
                        name: "FK_HabitacoesRating_Habitacoes_HabitacoesId",
                        column: x => x.HabitacoesId,
                        principalTable: "Habitacoes",
                        principalColumn: "Id");
                });

            migrationBuilder.CreateTable(
                name: "HabitacoesFotos",
                columns: table => new
                {
                    ID = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    Nome = table.Column<string>(type: "nvarchar(max)", nullable: false),
                    TipoFicheiro = table.Column<string>(type: "nvarchar(max)", nullable: false),
                    Extencao = table.Column<string>(type: "nvarchar(max)", nullable: false),
                    CreatedOn = table.Column<DateTime>(type: "datetime2", nullable: true),
                    Data = table.Column<byte[]>(type: "varbinary(max)", nullable: true),
                    Motivo = table.Column<string>(type: "nvarchar(max)", nullable: true),
                    HabitacoesId = table.Column<int>(type: "int", nullable: true),
                    ArrendamentosId = table.Column<int>(type: "int", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_HabitacoesFotos", x => x.ID);
                    table.ForeignKey(
                        name: "FK_HabitacoesFotos_Arrendamentos_ArrendamentosId",
                        column: x => x.ArrendamentosId,
                        principalTable: "Arrendamentos",
                        principalColumn: "Id");
                    table.ForeignKey(
                        name: "FK_HabitacoesFotos_Habitacoes_HabitacoesId",
                        column: x => x.HabitacoesId,
                        principalTable: "Habitacoes",
                        principalColumn: "Id");
                });

            migrationBuilder.CreateIndex(
                name: "IX_AspNetUsers_EmpresaId",
                table: "AspNetUsers",
                column: "EmpresaId");

            migrationBuilder.CreateIndex(
                name: "IX_Arrendamentos_EmpresaId",
                table: "Arrendamentos",
                column: "EmpresaId");

            migrationBuilder.CreateIndex(
                name: "IX_Arrendamentos_HabitacoesId",
                table: "Arrendamentos",
                column: "HabitacoesId");

            migrationBuilder.CreateIndex(
                name: "IX_Arrendamentos_UtilizadoresId",
                table: "Arrendamentos",
                column: "UtilizadoresId");

            migrationBuilder.CreateIndex(
                name: "IX_Habitacoes_EmpresaId",
                table: "Habitacoes",
                column: "EmpresaId");

            migrationBuilder.CreateIndex(
                name: "IX_HabitacoesFotos_ArrendamentosId",
                table: "HabitacoesFotos",
                column: "ArrendamentosId");

            migrationBuilder.CreateIndex(
                name: "IX_HabitacoesFotos_HabitacoesId",
                table: "HabitacoesFotos",
                column: "HabitacoesId");

            migrationBuilder.CreateIndex(
                name: "IX_HabitacoesRating_HabitacoesId",
                table: "HabitacoesRating",
                column: "HabitacoesId");

            migrationBuilder.CreateIndex(
                name: "IX_HabitacoesRating_UtilizadoresId",
                table: "HabitacoesRating",
                column: "UtilizadoresId");

            migrationBuilder.AddForeignKey(
                name: "FK_AspNetUsers_Empresa_EmpresaId",
                table: "AspNetUsers",
                column: "EmpresaId",
                principalTable: "Empresa",
                principalColumn: "Id");
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_AspNetUsers_Empresa_EmpresaId",
                table: "AspNetUsers");

            migrationBuilder.DropTable(
                name: "HabitacoesFotos");

            migrationBuilder.DropTable(
                name: "HabitacoesRating");

            migrationBuilder.DropTable(
                name: "Arrendamentos");

            migrationBuilder.DropTable(
                name: "Habitacoes");

            migrationBuilder.DropTable(
                name: "Empresa");

            migrationBuilder.DropIndex(
                name: "IX_AspNetUsers_EmpresaId",
                table: "AspNetUsers");

            migrationBuilder.DropColumn(
                name: "EmpresaId",
                table: "AspNetUsers");

            migrationBuilder.DropColumn(
                name: "NIF",
                table: "AspNetUsers");

            migrationBuilder.DropColumn(
                name: "NomeCompleto",
                table: "AspNetUsers");
        }
    }
}
